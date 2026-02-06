/*
 * modrecsoc.h
 *
 *  VexRV SoC
 */

#ifndef MODRECSOC_H_
#define MODRECSOC_H_

#include "timer.h"
#include "prescaler.h"
#include "interrupt.h"
#include "gpio.h"
#include "uart.h"

#define CORE_HZ 12000000

#define GPIO_A    ((Gpio_Reg*)(0xF0000000))
#define TIMER_PRESCALER ((Prescaler_Reg*)0xF0020000)
#define TIMER_INTERRUPT ((InterruptCtrl_Reg*)0xF0020010)
#define TIMER_A ((Timer_Reg*)0xF0020040)
#define TIMER_B ((Timer_Reg*)0xF0020050)
#define UART      ((Uart_Reg*)(0xF0010000))
#define UART_SAMPLE_PER_BAUD 5

// SoC On-Chip Memory
#define OC_MEM          0x80000000

// SoC/Accel Shared Memory
#define PLAN_MEM        0xF0080000
#define PLAN_MEM_SIZE   1 << 12
#define INPUT_MEM       0xF0081000
#define INPUT_MEM_SIZE  1 << 15
#define DATA_MEM        0xF0089200
#define DATA_MEM_SIZE   1 << 20
#define CONST_MEM       0xF0189200
#define CONST_MEM_SIZE  

#define SHARED_MEM_SIZE PLAN_MEM_SIZE + INPUT_MEM_SIZE + DATA_MEM_SIZE

// HyperBus
#define HYPERBUS        (0xF0080000 + SHARED_MEM_SIZE)

#endif /* MODRECSOC_H_ */
