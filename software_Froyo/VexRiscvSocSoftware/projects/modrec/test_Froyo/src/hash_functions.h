
// #ifndef FROST_LEVEL1_H
// #define FROST_LEVEL1_H

#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>

#include "cfu.h"
#include "riscv.h"
// #include "plan.h"
// #include "values.h"


#include <modrecsoc.h>

void sha512_transform(uint8_t mode, uint8_t offsetRd, uint8_t offsetWr, uint16_t size);

void sha512_update_transform(uint8_t offsetRd, uint8_t offsetWr, uint8_t init);

void SHA512_Pad(uint8_t offsetRd, uint8_t offsetWr, uint8_t numbytes, uint8_t overflow_bytes, uint8_t init);

void sha512_update(uint8_t offsetRd, uint8_t accumRd, uint8_t offsetWr, uint8_t inlen, uint8_t upto, uint8_t init);

void sha512_final(uint8_t offsetRd, uint8_t offsetWr, uint8_t numbytes);