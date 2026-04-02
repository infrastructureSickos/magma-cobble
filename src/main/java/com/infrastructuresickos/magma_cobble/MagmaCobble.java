package com.infrastructuresickos.magma_cobble;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(MagmaCobble.MOD_ID)
public class MagmaCobble {
    public static final String MOD_ID = "magma_cobble";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public MagmaCobble() {
        MinecraftForge.EVENT_BUS.register(new MagmaCobbleEventHandler());
        LOGGER.info("MagmaCobble initialized");
    }
}
