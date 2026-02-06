#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>


#include "cfu.h"
#include "riscv.h"
// #include "plan.h"
#include "values.h"
#include "frost_level1.h"
#include "blake2s.h"

#include <modrecsoc.h>

void generate_commitments(uint32_t offsetRd_secret, uint32_t offsetWr_commitment, uint8_t t){

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

    for (uint32_t i = 0; i <  16; i++){
		*(datamems_start_addr_XY[i]) = *(val_XY_base + i);
		*(datamems_start_addr_ZT[i]) = *(val_ZT_base + i);
		*(datamems_start_addr_scalar[i]) = *(val_scalar + i);
		*(datamems_start_addr_one[i]) = *(val_one + i);
		*(datamems_start_addr_zero[i]) = *(val_zero + i);
	}

    for (uint32_t i = 0; i < t; i++){
        for (uint32_t j = 0; j <  8; j++){
            *(datamems_start_addr_scalar[j]) = *(datamems_start_addr_scalar[j] + offsetRd_secret +i);
        }
        cfu_dmem_ctrl_frost;
        point_mul_scalar(0, offsetWr_commitment+i);
        cfu_mem_ctrl_cpu;
    }

}

void generate_dkg_challenge(uint8_t index, uint32_t offsetRd_challenge){

    uint32_t *datamem_A_addr = (uint32_t *) 0xF0089200;
	uint32_t *datamem_B_addr = (uint32_t *) 0xF0109200;

	uint32_t (*datamems_start_addr)[1024] = datamem_A_addr;

    cfu_mem_ctrl_cpu;

    for (uint32_t k = 0; k <  8; k++){
        *(datamems_start_addr[k+8]+210) = 0x00000000;
        *(datamems_start_addr[k]+210) = 0x00000000;
        *(datamems_start_addr[k+8]+212) = 0x00000000;
        *(datamems_start_addr[k]+212) = 0x00000000;
    }

    *(datamems_start_addr[15]+210) = index;
    *(datamems_start_addr[15]+212) = 0x444B4743; //DKGC in hex

    cfu_dmem_ctrl_sha;

    sha512_update(210, 220, 224, 1, 0, 1);
    sha512_update(204, 220, 224, 32, 1, 0);
	sha512_update(200, 220, 224, 32, 33, 0);
    sha512_update(212, 220, 224, 4, 65, 0);
	SHA512_Pad(220, 224, 69, 0, 0);

    cfu_dmem_ctrl_frost;

    scalar_mod252(224, offsetRd_challenge);

    cfu_mem_ctrl_cpu;
}

void generate_shares(uint8_t *context, uint32_t offsetRd_secret, uint32_t offsetWr_shares, uint32_t offsetWr_comm, uint8_t t, uint8_t n){

    uint32_t *datamem_A_addr = (uint32_t *) 0xF0089200;
	uint32_t *datamem_B_addr = (uint32_t *) 0xF0109200;

	uint32_t (*datamems_start_addr)[1024] = datamem_A_addr;

    uint8_t numCoeffs = t - 1;
    uint8_t coeffs[numCoeffs][32];

    uint8_t buf[2];
    uint8_t key[32];

    buf[0] = 70;
    buf[1] = 82;  // initial input is FR in hex then dec

    cfu_mem_ctrl_cpu;

    for(uint8_t i = 0; i < 32; i++){
        key[i] = context[i];
    }

    for (int i=0; i<numCoeffs; i++){
        key[i] = context[i]^(i+1);
        blake2s(coeffs[i], 32, context, 2, key, 32);
    }

    for (int j = 0; j < numCoeffs; j++) {
        for (uint32_t k = 0; k <  8; k++){
            *(datamems_start_addr[k+8]+offsetRd_secret+j+1) = (uint32_t)(coeffs[j][k]<<24)| (uint32_t)(coeffs[j][k+1]<<16) | (uint32_t)(coeffs[j][k+2]<<8) | (uint32_t)(coeffs[j][k+3]);
        }
    }

    for(uint8_t i = 1; i <= n; i++){

        for (uint32_t k = 0; k <  8; k++){
            *(datamems_start_addr[k+8]+101) = 0x00000000;
            *(datamems_start_addr[k]+101) = 0x00000000;
        }

        for (int j = numCoeffs-1; j>= 0; j--){
            for (uint32_t k = 0; k <  8; k++){
                *(datamems_start_addr[k+8]+100) = (uint32_t)(coeffs[j][4*k]<<24)| (uint32_t)(coeffs[j][4*k+1]<<16) | (uint32_t)(coeffs[j][4*k+2]<<8) | (uint32_t)(coeffs[j][4*k+3]);
                *(datamems_start_addr[k]+100) = 0x00000000;
            }
            cfu_dmem_ctrl_frost;
            scalar_add_scalar(100, 110);
            cfu_mem_ctrl_cpu;
            *(datamems_start_addr[7]+110) = (uint32_t)i;
            cfu_dmem_ctrl_frost;
            scalar_mul_mod252(110, 101);
            cfu_mem_ctrl_cpu;
        }

        for (uint32_t k = 0; k <  8; k++){
            *(datamems_start_addr[k]+102) = 0x00000000;
            *(datamems_start_addr[k+8]+102) = *(datamems_start_addr[k+8]+offsetRd_secret);
        }

        cfu_dmem_ctrl_frost;
        scalar_add_scalar(101, offsetWr_shares+i);

        cfu_mem_ctrl_cpu;


    }
    // If trusted dealer not present
    generate_commitments(offsetRd_secret, offsetWr_comm, t);

}

void keygen_begin(uint8_t *context, uint8_t t, uint8_t n, uint8_t index){


    uint32_t *datamem_A_addr = (uint32_t *) 0xF0089200;
	uint32_t *datamem_B_addr = (uint32_t *) 0xF0109200;

	uint32_t (*datamems_start_addr)[1024] = datamem_A_addr;

    uint8_t secret[32];
    uint8_t buf[2];
    uint8_t key[32];

    buf[0] = 70;
    buf[1] = 82;  // initial input is FR in hex then dec

    for(int i = 0; i < 32; i++){
        key[i] = context[i];
    }

    cfu_mem_ctrl_cpu;

    *(datamems_start_addr[15]+148) = 0x46444b47; // FDKG in hex

    blake2s(secret, 32, context, 2, key, 32);

    for (uint32_t k = 0; k <  8; k++){
        *(datamems_start_addr[k+8]+150) = (uint32_t)(secret[4*k]<<24)| (uint32_t)(secret[4*k+1]<<16) | (uint32_t)(secret[4*k+2]<<8) | (uint32_t)(secret[4*k+3]);
        *(datamems_start_addr[k]+150) = 0x00000000;
    }

    generate_shares(context, 150, 170, 190, t, n);

    cfu_dmem_ctrl_sha;

    sha512_update(150, 165, 195, 32, 0, 1);
	sha512_update(148, 165, 195, 4, 32, 0);
	SHA512_Pad(165, 198, 36, 0, 0);

    cfu_dmem_ctrl_frost;

    scalar_mod252(198, 199);

    cfu_mem_ctrl_cpu;

    uint32_t *datamem_scalar_addr = (uint32_t *) 0xF0089230;
	uint32_t (*datamems_start_addr_scalar)[1024] = datamem_scalar_addr;

    for (uint32_t j = 0; j <  8; j++){
        *(datamems_start_addr_scalar[j]) = *(datamems_start_addr_scalar[j] + 199);
    }

    cfu_dmem_ctrl_frost;

    point_mul_scalar(0, 200); //signature zkp

    cfu_mem_ctrl_cpu;

    for (uint32_t j = 0; j <  8; j++){
        *(datamems_start_addr_scalar[j]) = *(datamems_start_addr_scalar[j] + 150);
    }

    cfu_dmem_ctrl_frost;

    point_mul_scalar(0, 204); //public key

    cfu_mem_ctrl_cpu;

    generate_dkg_challenge(index, 216);

    cfu_mem_ctrl_cpu;

    for (uint32_t i = 0; i <  8; i++){
		*(datamems_start_addr[i]+216) = *(datamems_start_addr[i+8]+150);
    }

    cfu_dmem_ctrl_frost;

    scalar_mul_mod252(216, 218);

    cfu_mem_ctrl_cpu;

    for (uint32_t i = 0; i <  16; i++){
		*(datamems_start_addr[i]+219) = *(datamems_start_addr[i]+199);
    }

    cfu_dmem_ctrl_frost;

    scalar_add_scalar(218, 250); //dkg_commitment zkp



}



