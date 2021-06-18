/*
 * Copyright (c) 2018-2020 C4
 *
 * This file is part of Curios, a mod made for Minecraft.
 *
 * Curios is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Curios is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Curios.  If not, see <https://www.gnu.org/licenses/>.
 */

package top.theillusivec4.curiostest;

import java.util.Arrays;
import java.util.stream.Collectors;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;

@Mod(CuriosTest.MODID)
public class CuriosTest {

  public static final String MODID = "curios-test";
  public static final Logger LOGGER = LogManager.getLogger();

  public CuriosTest() {
    final IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
    eventBus.addListener(this::enqueue);
    eventBus.addListener(this::clientSetup);
  }

  private void clientSetup(final FMLClientSetupEvent evt) {

  }

  private void enqueue(final InterModEnqueueEvent evt) {
    InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE,
        () -> Arrays.stream(SlotTypePreset.values())
            .map(preset -> preset.getMessageBuilder().build()).collect(Collectors.toList()));
  }
}
