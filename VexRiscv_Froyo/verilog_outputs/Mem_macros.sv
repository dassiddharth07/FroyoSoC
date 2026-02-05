module DPMEM1024X32M4_mem (AA, D, BWEB, WEB, AB, REB, CLK, Q);
    input wire CLK, WEB, REB;
    input wire [9:0] AA, AB;
    input wire [31:0] D, BWEB;
    output wire [31:0] Q;

    reg [31:0]MEMORY[0:1023];
    reg [31:0] temp;

    assign Q = temp;

    always @(posedge CLK) begin
        temp <= (REB == 1'b0 && WEB == 1'b1) ? MEMORY[AB] : ((REB == 1'b0 && WEB == 1'b0)? {8'b0, 24'bX} : temp);

        if (WEB == 1'b0) begin
            MEMORY[AA] <= (~BWEB & D) | (BWEB & MEMORY[AA]);
        end
        else begin
            MEMORY[AA] <= MEMORY[AA];
        end
    end

endmodule

module DPMEM256X32M2_mem (AA, D, BWEB, WEB, AB, REB, CLK, Q);
    input wire CLK, WEB, REB;
    input wire [7:0] AA, AB;
    input wire [31:0] D, BWEB;
    output wire [31:0] Q;

    reg [31:0]MEMORY[0:255];
    reg [31:0] temp;

    assign Q = temp;

    always @(posedge CLK) begin
        temp <= (REB == 1'b0 && WEB == 1'b1) ? MEMORY[AB] : ((REB == 1'b0 && WEB == 1'b0)? {8'b0, 24'bX} : temp);

        if (WEB == 1'b0) begin
            MEMORY[AA] <= (~BWEB & D) | (BWEB & MEMORY[AA]);
        end
        else begin
            MEMORY[AA] <= MEMORY[AA];
        end
    end

endmodule

module SPMEM8192X32M8_mem (CLK, CEB, WEB, A, D, BWEB, Q);
    input wire CLK, WEB, CEB;
    input wire [12:0] A;
    input wire [31:0] D, BWEB;
    output wire [31:0] Q;

    reg [31:0]MEMORY[0:8191];
    reg [31:0] temp;

    assign Q = temp;

    always @(posedge CLK) begin
        temp <= (CEB == 1'b0 && WEB == 1'b1)? MEMORY[A] : temp;

        if (CEB == 1'b0 && WEB == 1'b0) begin
            MEMORY[A] <= (~BWEB & D) | (BWEB & MEMORY[A]);
        end
        else begin
            MEMORY[A] <= MEMORY[A];
        end
    end

endmodule