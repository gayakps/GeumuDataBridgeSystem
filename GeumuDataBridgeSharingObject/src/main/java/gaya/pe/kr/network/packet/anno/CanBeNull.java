package gaya.pe.kr.network.packet.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target( {ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER} )
public @interface CanBeNull {
}
