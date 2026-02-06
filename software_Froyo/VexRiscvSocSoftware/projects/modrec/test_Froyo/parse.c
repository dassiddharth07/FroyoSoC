#include <stdio.h>
#include <stdint.h>
#include <string.h>
#include <stdlib.h> // Required for EXIT_FAILURE

// Define constants for clarity
#define INPUT_LENGTH 128 // 512 bits / 4 bits_per_char = 128 characters
#define OUTPUT_SIZE 16
#define HEX_CHARS_PER_BLOCK 8 // 32 bits / 4 bits_per_char = 8 hex characters per uint32_t

/**
 * @brief Converts a single hexadecimal character ('0'-'9', 'a'-'f', 'A'-'F') to its integer value (0-15).
 *
 * @param hex_char The hexadecimal character to convert.
 * @return int The integer value (0-15), or -1 if the character is invalid.
 */
int hex_char_to_int(char hex_char) {
    if (hex_char >= '0' && hex_char <= '9') {
        return hex_char - '0';
    } else if (hex_char >= 'a' && hex_char <= 'f') {
        return hex_char - 'a' + 10;
    } else if (hex_char >= 'A' && hex_char <= 'F') {
        return hex_char - 'A' + 10;
    }
    return -1; // Invalid hex character
}


/**
 * @brief Converts a 128-character hexadecimal string (512 bits) into an array of 16 unsigned 32-bit integers.
 *
 * The function processes the input string in 16 blocks of 8 hexadecimal characters.
 * Each 8-character block is parsed in Big-Endian order (MSB first) and assembled
 * into a single 32-bit integer (uint32_t).
 *
 * @param input_string The 128-character null-terminated hexadecimal string.
 * @param hex_array The destination array of 16 uint32_t integers.
 * @return int 0 on success, or -1 on error (e.g., incorrect length or invalid character).
 */
int hex_string_to_uint32_array(const char *input_string, uint32_t hex_array[OUTPUT_SIZE]) {
    // 1. Check if the input string is the correct length
    if (strlen(input_string) != INPUT_LENGTH) {
        fprintf(stderr, "Error: Input string must be exactly %d hexadecimal characters long.\n", INPUT_LENGTH);
        return -1;
    }

    // 2. Iterate 16 times to generate 16 uint32_t numbers
    for (int i = 0; i < OUTPUT_SIZE; i++) {
        uint32_t block_value = 0;
        // Calculate the starting position of the current 8-character block
        int start_index = i * HEX_CHARS_PER_BLOCK;

        // 3. Iterate through the 8 hex characters within the current block (MSB to LSB)
        for (int j = 0; j < HEX_CHARS_PER_BLOCK; j++) {
            char hex_char = input_string[start_index + j];
            int nibble_value = hex_char_to_int(hex_char);

            if (nibble_value == -1) {
                fprintf(stderr, "Error: Invalid hexadecimal character found: '%c' at index %d.\n", hex_char, start_index + j);
                return -1;
            }

            // Accumulate the value by shifting the current block_value 4 bits to the left
            // (making space for the new nibble) and then OR-ing the new nibble in.
            // This ensures Big-Endian parsing of the hex string block.
            block_value = (block_value << 4) | (uint32_t)nibble_value;
        }

        // 4. Store the final 32-bit value in the output array
        hex_array[i] = block_value;
    }
    return 0; // Success
}

int main() {
    // Example 128-character hexadecimal string.
    // This example uses sequential hex values (01234567, 89ABCDEF, etc.) for easy verification.
    const char input_data[INPUT_LENGTH + 1] =
        // "00000000000000000000000000000000000000000000000000000000000000011878a0f02874899a9915b1719b541c82df0f607f88aead0a9221754c5a48224a";
        "000000000000000000000000000000000000000000000000000000000000000167875f0fd78b766566ea4e8e64abe37d20f09f80775152f56dde8ab3a5b7dda3";

    // Array to hold the 16 resulting 32-bit hexadecimal numbers
    uint32_t hex_results[OUTPUT_SIZE];

    printf("--- 512-bit Hex String to 16x 32-bit Array Converter ---\n");
    printf("Input Hex String (%d chars):\n\"%s\"\n\n", INPUT_LENGTH, input_data);

    // Perform the conversion and check for errors
    if (hex_string_to_uint32_array(input_data, hex_results) != 0) {
        return EXIT_FAILURE;
    }

    // Print the results
    printf("Output Array of %d Hexadecimal (uint32_t) Numbers:\n", OUTPUT_SIZE);
    printf("----------------------------------------\n");

    printf("uint32_t val_all = {");
    for (int i = 0; i < OUTPUT_SIZE; i++) {
        // %08X ensures 8 hex digits are printed, padded with leading zeros.
        printf(" 0x%08X,", hex_results[i]);
    }
    printf("\n");

    return 0;
}