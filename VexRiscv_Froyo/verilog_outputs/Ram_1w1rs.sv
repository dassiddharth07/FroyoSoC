module Ram_1w_1rs    #(
    parameter   [20:0]             wordCount=1024,
    parameter   [7:0]             wordWidth=32,
    parameter                     clockCrossing=0,
    parameter                    technology=0,
    parameter                     readUnderWrite=0,
    parameter                     wrAddressWidth=10,
    parameter                   wrDataWidth=0,
    parameter                   wrMaskWidth=4,
    parameter                   wrMaskEnable=0,
    parameter                   rdAddressWidth=10,
    parameter                   rdDataWidth=0
    )
    (
        // Read port
        input                           wr_clk,
        input                           wr_en,
        input [$clog2(wordCount)-1:0]   wr_addr,
        input [wrMaskWidth-1:0]         wr_mask,
        input [wrDataWidth-1:0]                    wr_data,
        //Write port
        input                           rd_clk,
        input                           rd_en,
        input [$clog2(wordCount)-1:0]   rd_addr,
        output [rdDataWidth-1:0]                   rd_data
    );

    genvar i;
    //localparam addrWidth = $clog2(wordCount);
    localparam num_banks = wordCount>>10;
    localparam extra_bits = 32 - wordWidth;

    //logic [3:0] maskb;
    logic [31:0] wr_bwe;

    //assign maskb = ~mask;
    generate
        if (wrMaskWidth == 1) begin
            assign wr_bwe = {32{wr_mask}};
        end
        else if (wrMaskWidth == 4) begin
            assign wr_bwe = { {8{wr_mask[3]}}, {8{wr_mask[2]}}, {8{wr_mask[1]}}, {8{wr_mask[0]}} };
        end
    endgenerate

    logic [31:0] qout [num_banks-1:0];

    generate
        if (wordWidth < 32) begin : gen_block // D/I-Cache Tags
            DPMEM256X32M2_mem sram_macros_0 (
                .AA      ({'0,wr_addr}   ), //i
                .D       ({{extra_bits{1'b0}},wr_data}   ), //i
                .BWEB    (~wr_bwe   ), //i
                .WEB     (~wr_en    ), //i
                .AB      ({'0,rd_addr}   ), //i
                .REB     (~rd_en    ), //i
                .CLK     (rd_clk    ), //i
                .Q       (rd_data   )  //o
            );
        end
        else if (num_banks == 1) begin : gen_block
            DPMEM1024X32M4_mem sram_macros_0 (
                .AA      (wr_addr   ), //i
                .D       (wr_data   ), //i
                .BWEB    (~wr_bwe   ), //i
                .WEB     (~wr_en    ), //i
                .AB      (rd_addr   ), //i
                .REB     (~rd_en    ), //i
                .CLK     (rd_clk    ), //i
                .Q       (rd_data   )  //o
            );
        end
        else begin : gen_block
            for (i=0; i < num_banks; i++) begin : gen_blocks
                DPMEM1024X32M4_mem sram_macros_0 (
                    .AA      (wr_addr   ), //i
                    .D       (wr_data   ), //i
                    .BWEB    (~wr_bwe   ), //i
                    .WEB     (~((i == wr_addr[wrAddressWidth-1:10]) & wr_en)    ), //i
                    .AB      (rd_addr   ), //i
                    .REB     (~((i == rd_addr[rdAddressWidth-1:10]) & rd_en)    ), //i
                    .CLK     (rd_clk    ), //i
                    .Q       (qout[i]   )  //o
                );
            end

            logic [rdAddressWidth-11:0] bank_addr_reg;
            always_ff @(posedge rd_clk) begin
                bank_addr_reg <= rd_addr[rdAddressWidth-1:10];
            end

            assign rd_data = qout[bank_addr_reg];
        end

    endgenerate

endmodule