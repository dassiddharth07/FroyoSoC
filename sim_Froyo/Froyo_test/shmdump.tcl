database -open -shm -into dump_new.shm waves -default
probe -create -database waves chip.chipCore.soc.axi_sharedRam -assertions -transaction -depth all
probe -create -shm chip.chipCore.soc.axi_cfuAccels.frostAccel -depth all
probe -create -shm chip.chipCore.soc.axi_ram -depth all
probe -all -memories -shm -depth all
probe -all -shm -depth 5
probe -ports -shm -depth 6
run
