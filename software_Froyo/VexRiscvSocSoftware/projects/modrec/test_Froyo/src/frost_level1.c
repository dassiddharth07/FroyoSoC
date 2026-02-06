#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>

#include "cfu.h"
#include "riscv.h"
// #include "plan.h"
#include "values.h"


#include <modrecsoc.h>

void frostaccel(uint8_t mode, uint8_t offsetRd, uint8_t offsetWr) {
	uint32_t all_bits = (uint32_t) (mode + (offsetRd << 4) + (offsetWr << 12));
	cfu_csr_write(FROST_CTRL_SIGNALS, all_bits | 0x80000000);
	cfu_csr_write(FROST_CTRL_SIGNALS, all_bits | 0x00000000);
	uint32_t tmp;
	while (1) {
		tmp = cfu_csr_read(FROST_STATUS_IO);
		// GPIO_A->OUTPUT = tmp;
		if(tmp & 0x1 == 1) break;
	}
	cfu_csr_write(FROST_CTRL_SIGNALS, all_bits | 0x40000000);
}

void scalar_mul_mod252(uint8_t offsetRd, uint8_t offsetWr) {
    cfu_select_manager;
	cfu_csr_write(FROST_CTRL_SIGNALS, 0x00000000);

	cfu_dmem_ctrl_frost;

    frostaccel(8, offsetRd, offsetWr);

    cfu_mem_ctrl_cpu;
}

void scalar_mul_full(uint8_t offsetRd, uint8_t offsetWr) {
    cfu_select_manager;
	cfu_csr_write(FROST_CTRL_SIGNALS, 0x00000000);

	cfu_dmem_ctrl_frost;

    frostaccel(9, offsetRd, offsetWr);

    cfu_mem_ctrl_cpu;
}

void scalar_mod252(uint8_t offsetRd, uint8_t offsetWr) {
    cfu_select_manager;
	cfu_csr_write(FROST_CTRL_SIGNALS, 0x00000000);

	cfu_dmem_ctrl_frost;

    frostaccel(10, offsetRd, offsetWr);

    cfu_mem_ctrl_cpu;
}

void mul_mod25519(uint8_t offsetRd, uint8_t offsetWr) {
    cfu_select_manager;
	cfu_csr_write(FROST_CTRL_SIGNALS, 0x00000000);

	cfu_dmem_ctrl_frost;

    frostaccel(4, offsetRd, offsetWr);

    cfu_mem_ctrl_cpu;
}

void add_mod25519(uint8_t offsetRd, uint8_t offsetWr) {
    cfu_select_manager;
	cfu_csr_write(FROST_CTRL_SIGNALS, 0x00000000);

	cfu_dmem_ctrl_frost;

    frostaccel(5, offsetRd, offsetWr);

    cfu_mem_ctrl_cpu;
}

void sub_mod25519(uint8_t offsetRd, uint8_t offsetWr) {
    cfu_select_manager;
	cfu_csr_write(FROST_CTRL_SIGNALS, 0x00000000);

	cfu_dmem_ctrl_frost;

    frostaccel(6, offsetRd, offsetWr);

    cfu_mem_ctrl_cpu;
}

void point_mul_scalar(uint8_t offsetRd, uint8_t offsetWr) {
    cfu_select_manager;
	cfu_csr_write(FROST_CTRL_SIGNALS, 0x00000000);

	cfu_dmem_ctrl_frost;

    frostaccel(0, offsetRd, offsetWr);

    cfu_mem_ctrl_cpu;
}

// point_mul_scalar(uint8_t offsetRd, uint8_t offsetWr) {
//     cfu_select_manager;
// 	cfu_csr_write(FROST_CTRL_SIGNALS, 0x00000000);

// 	cfu_dmem_ctrl_frost;

//     frostaccel(0, offsetRd, offsetWr);

//     cfu_mem_ctrl_cpu;
// }

void point_add_point(uint8_t offsetRd, uint8_t offsetWr) {
    cfu_select_manager;
	cfu_csr_write(FROST_CTRL_SIGNALS, 0x00000000);

	cfu_dmem_ctrl_frost;

    frostaccel(1, offsetRd, offsetWr);

    cfu_mem_ctrl_cpu;
}

void point_sub_point(uint8_t offsetRd, uint8_t offsetWr) {

    uint32_t *datamem_A_addr = (uint32_t *) 0xF0089200;

	uint32_t (*datamems_start_addr)[1024] = datamem_A_addr;


    cfu_select_manager;

    cfu_csr_write(FROST_CTRL_SIGNALS, 0x00000000);

    cfu_mem_ctrl_cpu;
    for (uint32_t i = 0; i <  8; i++){
		*(datamems_start_addr[i]+offsetRd+2) = 0;
        *(datamems_start_addr[i]+offsetRd+3) = 0;
        *(datamems_start_addr[i+8]+offsetRd+2) = *(datamems_start_addr[i]+offsetRd+0);
        *(datamems_start_addr[i+8]+offsetRd+3) = *(datamems_start_addr[i+8]+offsetRd+1);
	}

    cfu_dmem_ctrl_frost;

    frostaccel(6, offsetRd + 2, offsetRd + 4);

    cfu_mem_ctrl_cpu;

    for (uint32_t i = 0; i <  8; i++){
		*(datamems_start_addr[i]+offsetRd+6) = *(datamems_start_addr[i]+offsetRd+4);
        *(datamems_start_addr[i]+offsetRd+7) = *(datamems_start_addr[i]+offsetRd+1);
        *(datamems_start_addr[i+8]+offsetRd+6) = *(datamems_start_addr[i+8]+offsetRd+0);
        *(datamems_start_addr[i+8]+offsetRd+7) = *(datamems_start_addr[i+8]+offsetRd+4);
	}

    cfu_dmem_ctrl_frost;

    frostaccel(1, offsetRd + 6, offsetWr);

    cfu_mem_ctrl_cpu;

}

void base_mul_scalar(uint8_t offsetRd, uint8_t offsetWr) {
    uint32_t *datamem_A_addr = (uint32_t *) 0xF0089200;

	uint32_t (*datamems_start_addr)[1024] = datamem_A_addr;

    uint32_t *datamem_XY_addr = (uint32_t *) 0xF0089200;
	uint32_t *datamem_ZT_addr = (uint32_t *) 0xF0089204;

	uint32_t *datamem_scalar_addr = (uint32_t *) 0xF0089230;
	uint32_t *datamem_one_addr = (uint32_t *) 0xF0089238;
	uint32_t *datamem_zero_addr = (uint32_t *) 0xF008923C;

	uint32_t (*datamems_start_addr_XY)[1024] = datamem_XY_addr + offsetRd;
	uint32_t (*datamems_start_addr_ZT)[1024] = datamem_ZT_addr + offsetRd;
	uint32_t (*datamems_start_addr_scalar)[1024] = datamem_scalar_addr + offsetRd;
	uint32_t (*datamems_start_addr_one)[1024] = datamem_one_addr + offsetRd;
	uint32_t (*datamems_start_addr_zero)[1024] = datamem_zero_addr + offsetRd;

    *(datamem_A_addr + 0) = *(val_all + 15);

    for (uint32_t i = 0; i <  16; i++){
		*(datamems_start_addr_XY[i]) = *(val_XY_base + i);
		*(datamems_start_addr_ZT[i]) = *(val_ZT_base + i);
		*(datamems_start_addr_scalar[i]) = *(val_scalar + i);
		*(datamems_start_addr_one[i]) = *(val_one + i);
		*(datamems_start_addr_zero[i]) = *(val_zero + i);
	}


    cfu_select_manager;

    cfu_csr_write(FROST_CTRL_SIGNALS, 0x00000000);

    cfu_dmem_ctrl_frost;

    frostaccel(0, offsetRd, offsetWr);

    cfu_mem_ctrl_cpu;
}

void scalar_add_scalar(uint8_t offsetRd, uint8_t offsetWr) {

    uint32_t *datamem_A_addr = (uint32_t *) 0xF0089200;

	uint32_t (*datamems_start_addr)[1024] = datamem_A_addr;

    scalar_mod252(offsetRd, offsetRd+32);
    scalar_mod252(offsetRd+1, offsetRd+34);

    cfu_mem_ctrl_cpu;

    for (uint32_t i = 0; i <  8; i++){
		*(datamems_start_addr[i]+offsetRd+34) = *(datamems_start_addr[i+8]+offsetRd+32);
        *(datamems_start_addr[i]+offsetRd+33) = 0x00000000;
        *(datamems_start_addr[i+8]+offsetRd+33) = 0x00000000;
	}

    cfu_dmem_ctrl_frost;

    add_mod25519(offsetRd+33, offsetRd+34);

    scalar_mod252(offsetRd+34, offsetWr);

    cfu_mem_ctrl_cpu;

}

void scalar_sub_scalar(uint8_t offsetRd, uint8_t offsetWr) {
    // Store value of L at offset of 128

    uint32_t *datamem_A_addr = (uint32_t *) 0xF0089200;

	uint32_t (*datamems_start_addr)[1024] = datamem_A_addr;

    scalar_mod252(offsetRd, offsetRd+4);
    scalar_mod252(offsetRd+1, offsetRd+6);

    cfu_mem_ctrl_cpu;

    for (uint32_t i = 0; i <  8; i++){
		*(datamems_start_addr[i+8]+128) = *(datamems_start_addr[i]+offsetRd+4);
	}

    cfu_dmem_ctrl_frost;

    add_mod25519(128, offsetRd+8);

    cfu_mem_ctrl_cpu;

    for (uint32_t i = 0; i <  8; i++){
		*(datamems_start_addr[i+8]+offsetRd+6) = *(datamems_start_addr[i+8]+offsetRd+8);
        *(datamems_start_addr[i]+offsetRd+5) = 0x00000000;
        *(datamems_start_addr[i+8]+offsetRd+5) = 0x00000000;
	}

    cfu_dmem_ctrl_frost;

    sub_mod25519(offsetRd+5, offsetRd+6);

    scalar_mod252(offsetRd+6, offsetWr);

    cfu_mem_ctrl_cpu;

}

void scalar_sqmul(uint8_t offsetRd, uint8_t offsetWr, uint8_t offsetMulVal, uint32_t loopVal){

    uint32_t *datamem_A_addr = (uint32_t *) 0xF0089200;

	uint32_t (*datamems_start_addr)[1024] = datamem_A_addr;

    // cfu_mem_ctrl_cpu;

    // for (uint32_t i = 0; i <  8; i++){
	// 	*(datamems_start_addr[i]+offsetRd+1) = *(datamems_start_addr[i+8]+offsetRd);
    //     *(datamems_start_addr[i+8]+offsetRd+1) = *(datamems_start_addr[i+8]+offsetRd);
	// }

    cfu_dmem_ctrl_frost;

    for (uint32_t i = 0; i < loopVal; i++){
        scalar_mul_mod252(offsetRd, offsetRd);
    }

    cfu_mem_ctrl_cpu;

    for (uint32_t i = 0; i <  8; i++){
		*(datamems_start_addr[i]+offsetRd) = *(datamems_start_addr[i+8]+offsetMulVal);
	}

    cfu_dmem_ctrl_frost;

    scalar_mul_mod252(offsetRd, offsetWr);

    cfu_mem_ctrl_cpu;
}

void scalar_invert(uint8_t offsetRd, uint8_t offsetWr) {

    uint32_t *datamem_A_addr = (uint32_t *) 0xF0089200;

	uint32_t (*datamems_start_addr)[1024] = datamem_A_addr;

    cfu_mem_ctrl_cpu;

    for (uint32_t i = 0; i <  8; i++){
		*(datamems_start_addr[i]+offsetRd+49) = *(datamems_start_addr[i+8]+offsetRd);
        *(datamems_start_addr[i+8]+offsetRd+49) = *(datamems_start_addr[i+8]+offsetRd); // a 
	}

    cfu_dmem_ctrl_frost;
    scalar_mul_mod252(offsetRd+49, offsetRd+50); // a^2
    scalar_mul_mod252(offsetRd+50, offsetRd+51); // a^4
    scalar_mul_mod252(offsetRd+51, offsetRd+52); // a^8
    scalar_mul_mod252(offsetRd+52, offsetRd+53); // a^16
    scalar_mul_mod252(offsetRd+53, offsetRd+54); // a^32
    scalar_mul_mod252(offsetRd+54, offsetRd+55); // a^64
    cfu_mem_ctrl_cpu;

    for (uint32_t i = 0; i <  8; i++){
		*(datamems_start_addr[i]+offsetRd+50) = *(datamems_start_addr[i+8]+offsetRd+49); //  1 |  2 = 3
        *(datamems_start_addr[i]+offsetRd+52) = *(datamems_start_addr[i+8]+offsetRd+50); //  2 |  8 = 10
        *(datamems_start_addr[i]+offsetRd+55) = *(datamems_start_addr[i+8]+offsetRd+53); // 16 | 64 = 80
	}

    cfu_dmem_ctrl_frost;
    scalar_mul_mod252(offsetRd+50, offsetRd+56); // a^3
    scalar_mul_mod252(offsetRd+52, offsetRd+57); // a^10
    scalar_mul_mod252(offsetRd+55, offsetRd+58); // a^80
    cfu_mem_ctrl_cpu;

    for (uint32_t i = 0; i <  8; i++){
		*(datamems_start_addr[i]+offsetRd+49) = *(datamems_start_addr[i+8]+offsetRd+57); //  10 |  1 = 11
        *(datamems_start_addr[i]+offsetRd+58) = *(datamems_start_addr[i+8]+offsetRd+56); //  3 |  80 = 83
	}

    cfu_dmem_ctrl_frost;
    scalar_mul_mod252(offsetRd+49, offsetRd+59); // a^11
    scalar_mul_mod252(offsetRd+58, offsetRd+60); // a^83
    scalar_mul_mod252(offsetRd+59, offsetRd+61); // a^22
    cfu_mem_ctrl_cpu;

    for (uint32_t i = 0; i <  8; i++){
		*(datamems_start_addr[i]+offsetRd+61) = *(datamems_start_addr[i+8]+offsetRd+53); //  16 |  22 = 38
        *(datamems_start_addr[i]+offsetRd+60) = *(datamems_start_addr[i+8]+offsetRd+53); //  16 |  83 = 99
        *(datamems_start_addr[i]+offsetRd+55) = *(datamems_start_addr[i+8]+offsetRd+60); //  83 |  64 = 147
	}

    cfu_dmem_ctrl_frost;
    scalar_mul_mod252(offsetRd+61, offsetRd+62); // a^38
    scalar_mul_mod252(offsetRd+60, offsetRd+63); // a^99
    scalar_mul_mod252(offsetRd+55, offsetRd+64); // a^147
    cfu_mem_ctrl_cpu;

    for (uint32_t i = 0; i <  8; i++){
		*(datamems_start_addr[i]+offsetRd+51) = *(datamems_start_addr[i+8]+offsetRd+63); //  99 |  4 = 103
        *(datamems_start_addr[i]+offsetRd+52) = *(datamems_start_addr[i+8]+offsetRd+63); //  99 |  8 = 107
        *(datamems_start_addr[i]+offsetRd+64) = *(datamems_start_addr[i+8]+offsetRd+51); //  4 |  147 = 151
        *(datamems_start_addr[i]+offsetRd+55) = *(datamems_start_addr[i+8]+offsetRd+64); //  147 |  64 = 211
	}

    cfu_dmem_ctrl_frost;
    scalar_mul_mod252(offsetRd+51, offsetRd+65); // a^103
    scalar_mul_mod252(offsetRd+52, offsetRd+66); // a^107
    scalar_mul_mod252(offsetRd+64, offsetRd+67); // a^151
    scalar_mul_mod252(offsetRd+55, offsetRd+68); // a^211
    cfu_mem_ctrl_cpu;

    for (uint32_t i = 0; i <  8; i++){
		*(datamems_start_addr[i]+offsetRd+62) = *(datamems_start_addr[i+8]+offsetRd+67); //  151 |  38 = 189
        *(datamems_start_addr[i]+offsetRd+58) = *(datamems_start_addr[i+8]+offsetRd+67); //  151 |  80 = 231
	}

    cfu_dmem_ctrl_frost;
    scalar_mul_mod252(offsetRd+62, offsetRd+69); // a^189
    scalar_mul_mod252(offsetRd+58, offsetRd+70); // a^231
    cfu_mem_ctrl_cpu;

    for (uint32_t i = 0; i <  8; i++){
		*(datamems_start_addr[i]+offsetRd+70) = *(datamems_start_addr[i+8]+offsetRd+51); //  4 |  231 = 235
	}

    cfu_dmem_ctrl_frost;
    scalar_mul_mod252(offsetRd+70, offsetRd+71); // a^235
    cfu_mem_ctrl_cpu;

    for (uint32_t i = 0; i <  8; i++){
		*(datamems_start_addr[i]+offsetRd+71) = *(datamems_start_addr[i+8]+offsetRd+57); //  10 |  235 = 245
	}

    cfu_dmem_ctrl_frost;
    scalar_mul_mod252(offsetRd+71, offsetRd+72); // a^245
    cfu_mem_ctrl_cpu;

    for (uint32_t i = 0; i <  8; i++){
		*(datamems_start_addr[i]+offsetRd+72) = *(datamems_start_addr[i+8]+offsetRd+59); //  11 |  245 = 256
	}

    cfu_dmem_ctrl_frost;
    scalar_mul_mod252(offsetRd+72, offsetRd+73); // a^256
    cfu_mem_ctrl_cpu;

    scalar_sqmul(offsetRd+73, offsetRd+73, offsetRd+60, 126);
    scalar_sqmul(offsetRd+73, offsetRd+73, offsetRd+50, 9);

    for (uint32_t i = 0; i <  8; i++){
		*(datamems_start_addr[i]+offsetRd+73) = *(datamems_start_addr[i+8]+offsetRd+72); //  245 |  accum 
	}

    cfu_dmem_ctrl_frost;
    scalar_mul_mod252(offsetRd+73, offsetRd+73); // a^256
    cfu_mem_ctrl_cpu;

    scalar_sqmul(offsetRd+73, offsetRd+73, offsetRd+65, 7);
    scalar_sqmul(offsetRd+73, offsetRd+73, offsetRd+72, 9);
    scalar_sqmul(offsetRd+73, offsetRd+73, offsetRd+69, 11);
    scalar_sqmul(offsetRd+73, offsetRd+73, offsetRd+70, 8);

    scalar_sqmul(offsetRd+73, offsetRd+73, offsetRd+66, 9);
    scalar_sqmul(offsetRd+73, offsetRd+73, offsetRd+59, 6);
    scalar_sqmul(offsetRd+73, offsetRd+73, offsetRd+64, 14);
    scalar_sqmul(offsetRd+73, offsetRd+73, offsetRd+63, 10);

    scalar_sqmul(offsetRd+73, offsetRd+73, offsetRd+67, 9);
    scalar_sqmul(offsetRd+73, offsetRd+73, offsetRd+72, 10);
    scalar_sqmul(offsetRd+73, offsetRd+73, offsetRd+68, 8);
    scalar_sqmul(offsetRd+73, offsetWr, offsetRd+71, 8);

    cfu_mem_ctrl_cpu;

}

// void curve_from_hash(uint8_t offsetRd, uint8_t offsetWr) {
    
// }




