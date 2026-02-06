#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>

#include "cfu.h"
#include "riscv.h"
// #include "plan.h"
// #include "values.h"


#include <modrecsoc.h>

void compose_signature(uint8_t offsetRd, uint8_t offsetWr, uint8_t t);
void compute_binding_factors(uint32_t offsetRd_commitment_binding, uint32_t offsetRd_commitment_hiding, uint32_t offsetWr, uint32_t message_hash_offset,  uint8_t t);
void compute_group_commitment(uint32_t offsetRd_commitment_binding, uint32_t offsetRd_commitment_hiding, uint32_t offsetRd_binding, uint32_t offsetWr, uint8_t t);