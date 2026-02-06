#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>

#include "cfu.h"
#include "riscv.h"
// #include "plan.h"
#include "values.h"


#include <modrecsoc.h>

void sha512_transform(uint8_t mode, uint8_t offsetRd, uint8_t offsetWr, uint8_t no_update) {
	uint32_t all_bits = (uint32_t) (mode + (offsetRd << 4) + (offsetWr << 12) + (no_update << 3));
	cfu_csr_write(SHA_CTRL_SIGNALS_1, 0x000F0000); // set block size to 1024 bits
	cfu_csr_write(SHA_CTRL_SIGNALS_0, all_bits | 0x80000000);
	cfu_csr_write(SHA_CTRL_SIGNALS_0, all_bits | 0x00000000);
	uint32_t tmp;
	while (1) {
		tmp = cfu_csr_read(SHA_STATUS_IO);
		// GPIO_A->OUTPUT = tmp;
		if(tmp & 0x1 == 1) break;
	}
	cfu_csr_write(SHA_CTRL_SIGNALS_0, all_bits | 0x40000000);
}


void sha512_update_transform(uint8_t offsetRd, uint8_t offsetWr, uint8_t init) {
	cfu_select_manager;
	cfu_csr_write(SHA_CTRL_SIGNALS_0, 0x00000000);
	cfu_csr_write(SHA_CTRL_SIGNALS_1, 0x00000000);

	cfu_dmem_ctrl_sha;

	sha512_transform(3, offsetRd, offsetWr, init);

	cfu_mem_ctrl_cpu;
}


void SHA512_Pad(uint8_t offsetRd, uint8_t offsetWr, uint8_t totalbytes, uint8_t overflowbytes, uint8_t init)
{
    uint32_t r;
    // unsigned int i;

	uint8_t numbytes;

	numbytes = overflowbytes;

	uint8_t bytes_all[128];
	uint32_t memval;
	int i;

	uint8_t x = numbytes % 4;
	uint8_t xm = (x == 0) ? 0 : 4 - x;
	if (x != 0) {
		r = (uint32_t)(numbytes >> 2) + 1;
	} else {
		r = (uint32_t)(numbytes >> 2);
	}

	// printf("numbytes: %d, r: %d, xm: %d\n", numbytes, r, xm);

	uint8_t overflow_words = r % 16;
	// uint8_t addr = r >> 4;

	uint32_t *datamem_A_addr = (uint32_t *) 0xF0089200;

	uint32_t (*datamems_start_addr)[1024] = datamem_A_addr;

	cfu_mem_ctrl_cpu;

	int ctr = 0;

	// for(int p = r-1; p >= 0; p--) {
	// 	printf("memval[%d]: loop\n", p);
	// }

	for(int p = r-1; p >= 0; p--) {
		uint32_t x = (uint32_t)15 - (p % 16);
		uint32_t y = (uint32_t)p >> 4;
		memval = *(datamems_start_addr[x]+offsetRd+y);
		// memval = *(datamems_start_addr[15-(i % 16)]+offsetRd+(i >> 4));
		bytes_all[ctr] = (uint8_t)(memval >> 24);
		bytes_all[ctr + 1] = (uint8_t)(memval >> 16);
		bytes_all[ctr + 2] = (uint8_t)(memval >> 8);
		bytes_all[ctr + 3] = (uint8_t)(memval);
		ctr = ctr + 4;
		// printf("memval[%d]: loop\n", p);
	}

	// printf("After copying data, ctr: %d\n", ctr);

	bytes_all[ctr] = 0x80;
	bytes_all[ctr + 1] = 0x00;
	bytes_all[ctr + 2] = 0x00;
	bytes_all[ctr + 3] = 0x00;
	bytes_all[ctr + 4] = 0x00;
	bytes_all[ctr + 5] = 0x00;
	bytes_all[ctr + 6] = 0x00;
	bytes_all[ctr + 7] = 0x00;

	ctr = xm;
	uint32_t addr = 31;
	for(i = r+1; i >= 1; i--) {
		memval = (uint32_t)(bytes_all[ctr] << 24) | (uint32_t)(bytes_all[ctr + 1] << 16) | (uint32_t)(bytes_all[ctr + 2] << 8) | (uint32_t)(bytes_all[ctr + 3]);
		*(datamems_start_addr[15 - (addr % 16)]+offsetRd+10+(addr >> 4)) = memval;
		ctr = ctr + 4;
		addr = addr - 1;
		// printf("memval = %x\n", memval);
	}

	uint8_t checkInit = init;

	uint8_t checkoffset = 10;

	if (numbytes < 112) {
		for (i = 31 - (r+1); i >= 1 ; i--) {
			*(datamems_start_addr[15 - (addr % 16)]+offsetRd+10+(addr >> 4)) = 0x00000000;
			addr = addr - 1;
		}

		*(datamems_start_addr[15 - (addr % 16)]+offsetRd+10+(addr >> 4)) = (uint32_t)(totalbytes << 3);

	} 
	else {
		for (i = 32 - (r+1); i >= 1 ; i--) {
			*(datamems_start_addr[15 - (addr % 16)]+offsetRd+10+(addr >> 4)) = 0x00000000;
			addr = addr - 1;
		}

		for (int p = 0; p < 15; p++) {
			*(datamems_start_addr[(uint32_t)p]+offsetRd+12) = 0x00000000;
			*(datamems_start_addr[(uint32_t)p]+offsetRd+13) = 0x00000000;
		}

		*(datamems_start_addr[15]+offsetRd+12) = (uint32_t)(totalbytes << 3);
		*(datamems_start_addr[15]+offsetRd+13) = 0x00000000;


		cfu_dmem_ctrl_sha;

		sha512_update_transform(offsetRd+10, offsetRd+16, checkInit);

		checkInit = 0;

		checkoffset = 12;

	}

	sha512_update_transform(offsetRd + checkoffset, offsetWr, checkInit);

	cfu_mem_ctrl_cpu;
	

}


void sha512_update(uint8_t offsetRd, uint8_t accumRd, uint8_t offsetWr, uint8_t inlen, uint8_t upto, uint8_t init)
{

	uint32_t r, r_upto;

	uint8_t bytes_all[256];
	uint8_t bytes_new[256];
	uint32_t memval, addr, x, y;
	int i;

	uint8_t x1 = inlen % 4;
	uint8_t xm = (x1 == 0) ? 0 : 4 - x1;
	if (x1 != 0) {
		r = (uint32_t)(inlen >> 2) + 1;
	} else {
		r = (uint32_t)(inlen >> 2);
	}

	// printf("update start\n");

	uint8_t x_upto = upto % 4;
	uint8_t xm_upto = (x_upto == 0) ? 0 : 4 - x_upto;
	// uint8_t xm_upto = 4 - x_upto;
	if (x_upto != 0) {
		r_upto = (uint32_t)(upto >> 2) + 1;
	} else {
		r_upto = (uint32_t)(upto >> 2);
	}

	uint8_t y_mem_upto = (uint32_t)r_upto >> 5;
	uint8_t r_mem_upto;
	if (x_upto != 0) {
		r_mem_upto = (uint32_t)((upto % 128) >> 2) + 1;
	} else {
		r_mem_upto = (uint32_t)((upto % 128) >> 2);
	}

	uint32_t *datamem_A_addr = (uint32_t *) 0xF0089200;

	uint32_t (*datamems_start_addr)[1024] = datamem_A_addr;

	

	cfu_mem_ctrl_cpu;

	int ctr = 0;

	// printf("F1\n");

	// for(int p = r-1; p >= 0; p--) {
	// 	printf("memval[%d]: loop\n", p);
	// }
	if(r_mem_upto > 0){
		x = (uint32_t)15 - ((r_mem_upto-1) % 16);
		y = (uint32_t)(((r_mem_upto-1) >> 4) + (y_mem_upto << 1));
		// printf("%d\n", (y_mem_upto << 1));
		// printf("x=%d y=%d\n", x, y);

		// printf("F1\n");
		memval = *(datamems_start_addr[x]+accumRd+y);
		// printf("%x\n",memval);

		// printf("F1\n");

		for (int p = (3-xm_upto); p >=0; p--){
			bytes_all[ctr] = (uint8_t)(memval >> (8*p));
			ctr = ctr + 1;
			// printf("F1\n");
		}
	}
	

	for(int p = r_mem_upto-2; p >= 0; p--) {
		x = (uint32_t)15 - (p % 16);
		y = (uint32_t)(p >> 4) + (y_mem_upto << 1);
		memval = *(datamems_start_addr[x]+accumRd+y);
		// memval = *(datamems_start_addr[15-(i % 16)]+offsetRd+(i >> 4));
		bytes_all[ctr] = (uint8_t)(memval >> 24);
		bytes_all[ctr + 1] = (uint8_t)(memval >> 16);
		bytes_all[ctr + 2] = (uint8_t)(memval >> 8);
		bytes_all[ctr + 3] = (uint8_t)(memval);
		ctr = ctr + 4;
		// printf("memval[%d]: loop\n", p);
		
	}

	x = (uint32_t)15 - ((r-1) % 16);
	y = (uint32_t)(r-1) >> 4;
	memval = *(datamems_start_addr[x]+offsetRd+y);

	// printf("mem = %x ", memval);

	for (int p = (3-xm); p >=0; p--){
		bytes_all[ctr] = (uint8_t)(memval >> (8*p));
		ctr = ctr + 1;
		
	}



	for(int p = r-2; p >= 0; p--) {
		x = (uint32_t)15 - (p % 16);
		y = (uint32_t)p >> 4;
		memval = *(datamems_start_addr[x]+offsetRd+y);
		// memval = *(datamems_start_addr[15-(i % 16)]+offsetRd+(i >> 4));
		bytes_all[ctr] = (uint8_t)(memval >> 24);
		bytes_all[ctr + 1] = (uint8_t)(memval >> 16);
		bytes_all[ctr + 2] = (uint8_t)(memval >> 8);
		bytes_all[ctr + 3] = (uint8_t)(memval);
		ctr = ctr + 4;
		// printf("memval[%d]: loop\n", p);
		// printf("mem = %x ", memval);
		
	}

	int total_bytes = ctr;
	int left_bytes = ctr;

	// int wr_bytes = (ctr >> 2)+1)

	// ctr = ctr - 4;
	if (inlen < 128 - (upto%128)){

		// printf("enter if loop\n");

		addr = (uint32_t)0;
		int remaining_bytes = ctr;
		for (int p = ctr-1; p > 0; p=p-4) {
			memval = (uint32_t)(bytes_all[p-3] << 24) | (uint32_t)(bytes_all[p-2] << 16) | (uint32_t)(bytes_all[p-1] << 8) | (uint32_t)(bytes_all[p]);
			*(datamems_start_addr[15 - (addr % 16)]+accumRd+(y_mem_upto << 1)+(addr >> 4)) = memval;
			addr = addr + 1;
			remaining_bytes = remaining_bytes - 4;
		}

		memval = (uint32_t)0;

		for (int p = 0; p<remaining_bytes; p++){
			memval = (memval << 8) | bytes_all[p];
		}

		*(datamems_start_addr[15 - (addr % 16)]+accumRd+(y_mem_upto << 1)+(addr >> 4)) = memval;



		// uint32_t x = (uint32_t)15 - (r_upto % 16);
		// uint32_t y = (uint32_t)r_upto >> 4;
		// memval = *(datamems_start_addr[x]+accumRd+y);
		// memval = memval << (xm_upto * 8);
		// for (int p = 0; p < xm_upto; p++) {
		// 	memval = memval >> 8 | (uint32_t)(bytes_all[ctr] << 24);
		// 	ctr = ctr + 1;
		// }
		// *(datamems_start_addr[x]+accumRd+y) = memval;

		// addr = (uint32_t)(r_upto+1);
		// for(i = 0; i < (r+1); i++) {
		// 	memval = (uint32_t)(bytes_all[ctr]) | (uint32_t)(bytes_all[ctr + 1] << 8) | (uint32_t)(bytes_all[ctr + 2] << 16) | (uint32_t)(bytes_all[ctr + 3] << 24);
		// 	*(datamems_start_addr[15 - (addr % 16)]+accumRd+(addr >> 4)) = memval;
		// 	ctr = ctr + 4;
		// 	addr = addr + 1;
		// }
	}
	else {
		ctr = 0;
		int offset_per_128B = 0;
		uint8_t no_update = init;

		// printf("enter else loop\n");

		while(left_bytes >= 128) {
			addr = 31;
			for(i = 31; i >= 0; i--) {
				memval = (uint32_t)(bytes_all[ctr] << 24) | (uint32_t)(bytes_all[ctr + 1] << 16) | (uint32_t)(bytes_all[ctr + 2] << 8) | (uint32_t)(bytes_all[ctr + 3]);
				*(datamems_start_addr[15 - (i % 16)]+accumRd+(y_mem_upto << 1)+offset_per_128B+(i >> 4)) = memval;
				ctr = ctr + 4;
				// printf("mem = %x ", memval);
			}

			cfu_dmem_ctrl_sha;

			sha512_update_transform( accumRd + (y_mem_upto << 1), offsetWr, no_update);

			cfu_mem_ctrl_cpu;

			offset_per_128B = offset_per_128B + 2;

			left_bytes = left_bytes-128;

			no_update = 0;
		}

		addr = (uint32_t)0;
		int words = total_bytes >> 7;
		int remaining_bytes = left_bytes;
		// printf("total_bytes = %d", total_bytes);
		int byte_loop = total_bytes-1;
		for (int p = 128*words + left_bytes - 1; p > 128*words; p=p-4) {
			memval = (uint32_t)(bytes_all[p-3] << 24) | (uint32_t)(bytes_all[p-2] << 16) | (uint32_t)(bytes_all[p-1] << 8) | (uint32_t)(bytes_all[p]);
			*(datamems_start_addr[15 - (addr % 16)]+accumRd+(y_mem_upto << 1)+offset_per_128B+(addr >> 4)) = memval;
			addr = addr + 1;
			remaining_bytes = remaining_bytes - 4;
			// printf("mem1 = %x %dp", memval, p);
		}

		// printf("new remaining_bytes = %d", remaining_bytes);

		memval = (uint32_t)0;

		for (int p = 0; p<remaining_bytes; p++){
			memval = (memval << 8) | bytes_all[p];
		}

		*(datamems_start_addr[15 - (addr % 16)]+accumRd+(y_mem_upto << 1)+offset_per_128B+(addr >> 4)) = memval;
		// printf("mem2 = %x ", memval);

		// printf("\n");


		// cfu_dmem_ctrl_sha;

		// sha512_update_transform(accumRd, accumRd + (y_mem_upto << 1), init);

		// cfu_mem_ctrl_cpu;

		// // to be compleed

		// inlen = inlen - (128 - upto);

		// uint8_t block_offset = (uint8_t)2;

		// while (inlen >= 128) {
		// 	sha512_update_transform(accumRd + block_offset, accumRd+16, 0);
		// 	inlen = inlen - 128;
		// 	block_offset = block_offset + 2;
		// }
	}
	
}
