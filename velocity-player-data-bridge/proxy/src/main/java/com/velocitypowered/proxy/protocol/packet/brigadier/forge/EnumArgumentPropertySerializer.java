/*
 * Copyright (C) 2018 Velocity Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.velocitypowered.proxy.protocol.packet.brigadier.forge;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.packet.brigadier.ArgumentPropertySerializer;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An argument property serializer that will serialize and deserialize nothing.
 */
public class EnumArgumentPropertySerializer implements ArgumentPropertySerializer<EnumArgumentProperty> {

  public static final EnumArgumentPropertySerializer ENUM = new EnumArgumentPropertySerializer();

  private EnumArgumentPropertySerializer() {
  }

  @Override
  public @Nullable EnumArgumentProperty deserialize(ByteBuf buf, ProtocolVersion protocolVersion) {
    return new EnumArgumentProperty(ProtocolUtils.readString(buf));
  }

  @Override
  public void serialize(EnumArgumentProperty object, ByteBuf buf, ProtocolVersion protocolVersion) {
    ProtocolUtils.writeString(buf, object.getClassName());
  }
}
