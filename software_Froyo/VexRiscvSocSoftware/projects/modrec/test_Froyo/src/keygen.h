#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>

#include "cfu.h"
#include "riscv.h"

generate_commitments(uint32_t offsetRd_secret, uint32_t offsetWr_commitment, uint8_t t);
void keygen_begin(uint8_t *context, uint8_t t, uint8_t n, uint8_t index);
void distributed_key_generation(uint32_t offsetRd_context, uint32_t offsetZero, uint8_t t, uint8_t n);