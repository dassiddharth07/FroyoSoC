/*
 * Move definitions to a single C file to avoid multiple-definition
 * linker errors. Use `extern` declarations here.
 */

#ifndef VALUES_H
#define VALUES_H

#include <stdint.h>

// extern declarations for shared values
extern uint32_t val_all[16];
// extern uint32_t val_all1[16];
extern uint32_t val_XY[16];
extern uint32_t val_ZT[16];

extern uint32_t val_XY_base[16];
extern uint32_t val_ZT_base[16];

extern uint32_t val_scalar[16];

extern uint32_t val_one[16];
extern uint32_t val_zero[16];
extern uint32_t val_one_p3[16];
extern uint32_t val_zero_p3[16];
extern uint32_t val_inv_scalar[8];
extern uint32_t val_sha_input_0[16];
extern uint32_t val_sha_input_1[16];
extern uint32_t val_sha_input_2[16];
extern uint32_t val_sha_input_3[16];

#endif /* VALUES_H */