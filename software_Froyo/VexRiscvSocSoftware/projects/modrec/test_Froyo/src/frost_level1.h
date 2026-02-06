
#ifndef FROST_LEVEL1_H
#define FROST_LEVEL1_H

#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>

#include "cfu.h"
#include "riscv.h"
// #include "plan.h"
// #include "values.h"


#include <modrecsoc.h>

/* CFU helper wrappers implemented in frost_level1.c */
void frostaccel(uint8_t mode, uint8_t offsetRd, uint8_t offsetWr);

/* Scalar and point operations */
void scalar_mul_mod252(uint8_t offsetRd, uint8_t offsetWr);
void scalar_mul_full(uint8_t offsetRd, uint8_t offsetWr);
void scalar_mod252(uint8_t offsetRd, uint8_t offsetWr);
void mul_mod25519(uint8_t offsetRd, uint8_t offsetWr);
void add_mod25519(uint8_t offsetRd, uint8_t offsetWr);
void sub_mod25519(uint8_t offsetRd, uint8_t offsetWr);

void point_mul_scalar(uint8_t offsetRd, uint8_t offsetWr);
void point_add_point(uint8_t offsetRd, uint8_t offsetWr);
void point_sub_point(uint8_t offsetRd, uint8_t offsetWr);

void base_mul_scalar(uint8_t offsetRd, uint8_t offsetWr);

/* Scalar arithmetic routines */
void scalar_add_scalar(uint8_t offsetRd, uint8_t offsetWr);
void scalar_sub_scalar(uint8_t offsetRd, uint8_t offsetWr);

#endif /* FROST_LEVEL1_H */

