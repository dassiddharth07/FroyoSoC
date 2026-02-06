#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>


#include "cfu.h"
#include "riscv.h"
// #include "plan.h"
#include "values.h"
#include "frost_level1.h"


#include <modrecsoc.h>

void compose_signature(uint8_t offsetRd, uint8_t offsetWr, uint8_t t) {

    // uint32_t *datamem_zero_addr = (uint32_t *) 0xF008923C;

    // uint32_t (*datamems_start_addr_zero)[1024] = datamem_zero_addr;

    uint32_t *datamem_A_addr = (uint32_t *) 0xF0089200;

	uint32_t (*datamems_start_addr)[1024] = datamem_A_addr;


    for (uint32_t i = 0; i <  16; i++){
		*(datamems_start_addr[i]+offsetRd+32) = 0;
	}

    cfu_dmem_ctrl_frost;

    for (uint32_t i = 0; i < (uint32_t)t; i++){
        scalar_add_scalar(offsetRd+i, offsetRd+i+1);
    }

    cfu_mem_ctrl_cpu;
    compute_binding_factors(64, 128, 640, 400,  t);
    compute_group_commitment(64, 128, 640, offsetWr, t);
}

void compute_binding_factors(uint32_t offsetRd_commitment_binding, uint32_t offsetRd_commitment_hiding, uint32_t offsetWr, uint32_t message_hash_offset,  uint8_t t){

    uint32_t *datamem_A_addr = (uint32_t *) 0xF0089200;
	uint32_t *datamem_B_addr = (uint32_t *) 0xF0109200;
    cfu_mem_ctrl_cpu;

	uint32_t (*datamems_start_addr)[1024] = datamem_A_addr;

    uint32_t i;

    int init = 1;
    cfu_dmem_ctrl_sha;

    for (i = 0; i < (uint32_t)t; i++){
        sha512_update(offsetRd_commitment_binding+i*2, offsetRd_commitment_hiding+32, offsetWr-8, 32, i*64, init);
        init = 0;
        sha512_update(offsetRd_commitment_hiding+i*2, offsetRd_commitment_hiding+32, offsetWr-8, 32, i*64+32, init);
    }
    SHA512_Pad(offsetRd_commitment_hiding+32+i+2, offsetWr, t*64, 0, init);

    init = 1;

    for (i = 0; i < (uint32_t)t; i++){
        sha512_update(message_hash_offset, offsetRd_commitment_hiding+64, offsetWr+i, 64, i*128, init);
        init = 0;
        sha512_update(offsetWr-8, offsetRd_commitment_hiding+64, offsetWr+i, 64, i*128+64, init);
        SHA512_Pad(offsetRd_commitment_hiding+64+i+2, offsetWr+i, t*128, 0, init);
        init = 1;
        cfu_dmem_ctrl_frost;
        scalar_mod252(offsetWr+i, offsetWr+i);
        cfu_dmem_ctrl_sha;
    }

    cfu_mem_ctrl_cpu;
}

void compute_group_commitment(uint32_t offsetRd_commitment_binding, uint32_t offsetRd_commitment_hiding, uint32_t offsetRd_binding, uint32_t offsetWr, uint8_t t){

    uint32_t i;

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

    cfu_mem_ctrl_cpu;

    for (uint32_t j = 0; j <  16; j++){
		*(datamems_start_addr[j]+offsetWr+4) = *(val_one_p3 + j);
		*(datamems_start_addr[j]+offsetWr+5) = *(val_zero_p3 + j);
	}

    for (i = 0; i < (uint32_t)t; i++){
        for (uint32_t j = 0; j <  16; j++){
		    *(datamems_start_addr[j]) = *(datamems_start_addr[j]+offsetRd_commitment_binding+i);
            *(datamems_start_addr[j]+1) = *(datamems_start_addr[j]+offsetRd_commitment_binding+i+1);
            *(datamems_start_addr_scalar[j]) = *(datamems_start_addr[j]+offsetRd_binding+i);
	    }
        cfu_dmem_ctrl_frost;
        point_mul_scalar(0, offsetWr);
        cfu_mem_ctrl_cpu;
        for (uint32_t j = 0; j <  16; j++){
		    *(datamems_start_addr[j]+offsetWr+2) = *(datamems_start_addr[j]+offsetRd_commitment_hiding+i);
            *(datamems_start_addr[j]+offsetWr+3) = *(datamems_start_addr[j]+offsetRd_commitment_hiding+i+1);
	    }
        cfu_dmem_ctrl_frost;
        point_add_point(offsetWr, offsetWr+2);
        point_add_point(offsetWr+2, offsetWr+4);
        cfu_mem_ctrl_cpu;
    }
}

