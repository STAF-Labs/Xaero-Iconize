package ru.lotuze.xaero_iconize;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;


@Mod(XaeroIconize.MODID)
public class XaeroIconize {

    public static final String MODID = "xaero_iconize";
    public static final Logger LOGGER = LogUtils.getLogger();

    public XaeroIconize(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("XAero iconize installed");
    }
}
