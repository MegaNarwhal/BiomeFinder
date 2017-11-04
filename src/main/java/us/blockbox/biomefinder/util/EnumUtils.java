package us.blockbox.biomefinder.util;

import com.google.common.base.Enums;
import com.google.common.base.Optional;

import java.util.EnumSet;
import java.util.Set;

public enum EnumUtils{
	;

	public static <E extends Enum<E>> Set<E> getByNames(Class<E> enumClass,String... names){
		Set<E> set = EnumSet.noneOf(enumClass);
		for(String name : names){
			Optional<E> optional = Enums.getIfPresent(enumClass,name);
			if(optional.isPresent()){
				set.add(optional.get());
			}
		}
		return set;
	}

	/**
	 * @param enumClass  The enum to search
	 * @param preference A listing of the desired enum names, in order from most to least preferred
	 * @return The most preferable constant present in the enum, or null if none were matched
	 */
	public static <E extends Enum<E>> E getWithFallbacks(Class<E> enumClass,String... preference){
		for(String s : preference){
			Optional<E> optional = Enums.getIfPresent(enumClass,s);
			if(optional.isPresent()){
				return optional.get();
			}
		}
		return null;
	}
}
