#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>

#include "cfu.h"
#include "riscv.h"
// #include "plan.h"
#include "values.h"


#include <modrecsoc.h>
#include "frost_level1.h"
#include "hash_functions.h"

void clkgen_config(uint8_t div, uint8_t coarse, uint8_t fast_clk){ 

	if (div > 9) div = 9;
	if (div <= 0) div = 1;
	uint8_t divmap[10] = {8, 1, 0, 3, 2, 0xD, 0xC, 0xF, 0xE, 9};
	uint8_t divider = divmap[div-1]<<6;
	
	if (coarse > 23) coarse = 23;

	cfu_csr_write(CLKGEN_1, 1<<coarse);
	cfu_csr_write(CLKGEN_2, 0x49224900);


	// Clkgen fast clock select
	// 11: ro_enable, 10: clkDiv, 0:clkGateEn (needs to be 1), 
	if (fast_clk > 0){ 
		cfu_csr_write(CLKGEN_0, 0xE0000C01 + (uint32_t)divider); // MSB: clkgen_select, fstclksel
		cfu_csr_write(CLKGEN_0, 0xC0000C01 + (uint32_t)divider);
	}
	else {
		cfu_csr_write(CLKGEN_0, 0x20000C01); // MSB: clkgen_select, fstclksel
		cfu_csr_write(CLKGEN_0, 0x00000C01);
	}
}

void dma_transfer(uint32_t* read_addr, uint32_t* write_addr, uint32_t word_count){
	uint32_t reduced_word_count = word_count-1;
	cfu_csr_write(DMA_RD_ADDR, read_addr);
	cfu_csr_write(DMA_WR_ADDR, write_addr);
	cfu_csr_write(DMA_CTRL, reduced_word_count | 0x80000000);
	uint32_t tmp;
	while (1) {
		tmp = cfu_csr_read(DMA_STATUS);
		// GPIO_A->OUTPUT = tmp;
		if(tmp & 0x1 == 1) break;
	}
	cfu_csr_write(DMA_CTRL, reduced_word_count);
}

//mode: 0000 - scalarmult, 0001 - point addition 
//mode: 1000 - mulmod 252, 1001 - mulfull, 1010 - mod 252
//mode: 0100 - mul25519, 0101 - add25519, 0110 - sub25519
// void frostaccel(uint8_t mode, uint8_t offsetRd, uint8_t offsetWr) {
// 	uint32_t all_bits = (uint32_t) (mode + (offsetRd << 4) + (offsetWr << 12));
// 	cfu_csr_write(FROST_CTRL_SIGNALS, all_bits | 0x80000000);
// 	cfu_csr_write(FROST_CTRL_SIGNALS, all_bits | 0x00000000);
// 	uint32_t tmp;
// 	while (1) {
// 		tmp = cfu_csr_read(FROST_STATUS_IO);
// 		// GPIO_A->OUTPUT = tmp;
// 		if(tmp & 0x1 == 1) break;
// 	}
// 	cfu_csr_write(FROST_CTRL_SIGNALS, all_bits | 0x40000000);
// }

void main() {
	
	// interruptCtrl_init(TIMER_INTERRUPT);
	// cfu_csr_write(CLKGEN_0, 0xE000043F);
	
	GPIO_A->OUTPUT_ENABLE = 0x000000FF;
	//GPIO_A->OUTPUT = 0x00000000 + CFU_STATE_INDEX_CSR_ADDR;

	
	GPIO_A->OUTPUT = 0x000000F7;

	GPIO_A->OUTPUT = 0x000000F7;



	uint32_t *datamem_A_addr = (uint32_t *) 0xF0089200;
	uint32_t *datamem_B_addr = (uint32_t *) 0xF0109200;

	uint32_t (*datamems_start_addr)[1024] = datamem_A_addr;


	uint32_t *datamem_XY_addr = (uint32_t *) 0xF0089200;
	uint32_t *datamem_ZT_addr = (uint32_t *) 0xF0089204;
	uint32_t *datamem_JK_addr = (uint32_t *) 0xF0089208;

	uint32_t *datamem_scalar_addr = (uint32_t *) 0xF0089230;
	uint32_t *datamem_one_addr = (uint32_t *) 0xF0089238;
	uint32_t *datamem_zero_addr = (uint32_t *) 0xF008923C;

	uint32_t (*datamems_start_addr_XY)[1024] = datamem_XY_addr;
	uint32_t (*datamems_start_addr_ZT)[1024] = datamem_ZT_addr;
	uint32_t (*datamems_start_addr_JK)[1024] = datamem_JK_addr;
	uint32_t (*datamems_start_addr_scalar)[1024] = datamem_scalar_addr;
	uint32_t (*datamems_start_addr_one)[1024] = datamem_one_addr;
	uint32_t (*datamems_start_addr_zero)[1024] = datamem_zero_addr;

	uint32_t *datamem_invert_scalar_addr = (uint32_t *) 0xF0089218;
	uint32_t (*datamems_start_addr_inv_scalar)[1024] = datamem_invert_scalar_addr;


	*(datamem_A_addr + 0) = *(val_all + 15);
	// *(datamem_A_addr + 1) = *(val_all + 14);

	// printf("Here %x ", *(datamem_A_addr));

	// uint32_t *plan_ptr1 = (uint32_t *) 0xF0080000;
	// *(plan_ptr1 + 0) = *(val_all + 0);

	// uint32_t *input_ptr1 = (uint32_t *) 0xF0081000;
	// *(input_ptr1 + 0) = *(val_all + 1);

	// *(ocmem_ptr1+0) = 0xAAAAAAAA;



	// for (uint32_t i = 0; i <  16; i++){
	// 	*(datamems_start_addr[i]) = *(val_all + i);
	// }

	// for (uint32_t i = 0; i <  16; i++){
	// 	*(datamems_start_addr[i]) = *(val_all + i);
	// }

	for (uint32_t i = 0; i <  16; i++){
		*(datamems_start_addr_XY[i]) = *(val_XY_base + i);
		*(datamems_start_addr_ZT[i]) = *(val_ZT_base + i);
		*(datamems_start_addr_scalar[i]) = *(val_scalar + i);
		*(datamems_start_addr_one[i]) = *(val_one + i);
		*(datamems_start_addr_zero[i]) = *(val_zero + i);
	}

	for (uint32_t i = 0; i < 8; i++){
		*(datamems_start_addr_inv_scalar[i+8]) = *(val_inv_scalar + i);
	}

	// for (uint32_t i = 0; i <  16; i++){
	// 	*(datamems_start_addr_XY[i]) = *(val_sha_input_0 + i);
	// 	*(datamems_start_addr_ZT[i]) = *(val_sha_input_1 + i);
	// }

	// for (uint32_t i = 0; i <  16; i++){
	// 	*(datamems_start_addr_XY[i]) = *(val_sha_input_2 + i);
	// 	*(datamems_start_addr_ZT[i]) = *(val_sha_input_3 + i);
	// }

	// for (uint32_t i = 0; i <  16; i++){
	// 	*(datamems_start_addr_XY[i]) = *(val_sha_input_1 + i);
	// 	*(datamems_start_addr_ZT[i]) = *(val_sha_input_2 + i);
	// 	*(datamems_start_addr_JK[i]) = *(val_sha_input_3 + i);
	// }

	uint8_t mode = 0;

    
	// Move plan to SSCA Accel plan mem
    cfu_select_manager;
	cfu_csr_write(FROST_CTRL_SIGNALS, 0x00000000);
	// cfu_csr_write(SHA_CTRL_SIGNALS_0, 0x00000000);

	cfu_dmem_ctrl_frost;
	// cfu_dmem_ctrl_sha;

	cfu_csr_write(CLKGEN_0, 0x00000000);

	// point_mul_scalar(0, 8);

	// sha512_update(0, 12, 8, 136, 0, 1);
	// sha512_update(2, 12, 8, 8, 136, 0);
	// SHA512_Pad(14, 8, 144, 16, 0);

	uint8_t val_context[32] = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
							 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10,
							 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18,
							 0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F, 0x20};

	keygen_begin((uint8_t *) val_context, 3, 4, 2);



	// sha512_transform(3, 0, 8, 1);
	// SHA512_Pad(0, 8, 104, 1);
	// scalar_add_scalar(0, 8);
	// scalar_invert(6, 8);

	// frostaccel(mode, 0, 8);

	// scalarmult(mode);

	cfu_mem_ctrl_cpu;

	// scalar_add_scalar(0, 8);

	// printf("Here %x ", *(datamem_A_addr));

	// printf("Result: ");
	for (uint32_t i = 0; i <  16; i++){
		printf("Here %x ", *(datamems_start_addr[i] + 8));
	}
	// printf("Here %x ", *(datamem_A_addr));
	printf("\n");

	puts("end of test working\n");



	
}


