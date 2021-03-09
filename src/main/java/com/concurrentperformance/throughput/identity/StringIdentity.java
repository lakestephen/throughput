package com.concurrentperformance.throughput.identity;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * System wide base class used to identify an item.
 * See Also {@link ib.internal.neon.core.precondition.IdentityPrecondition}
 *
 * @author Steve Lake
 */
@Immutable
public final class StringIdentity implements Identity { //TODO where should this live?

	private final String name;


	/**
	 * This constructor does not check the format of the StringIdentity,
	 * for speed as these will bve created a lot. However, it is advisable
	 * to use {@link ib.internal.neon.core.precondition.IdentityPrecondition}
	 * to check the format for user entered data.
	 */
	public StringIdentity(@Nonnull String name) {
		this(name, false);
	}

	/**
	 * This constructor does not check the format of the StringIdentity,
	 * for speed as these will bve created a lot. However, it is advisable
	 * to use {@link ib.internal.neon.core.precondition.IdentityPrecondition}
	 * to check the format for user entered data.
	 * Setting appendUUID to true will ensure that your name globally unique
	 * (to the tolerances defined in {@link UUID}) by using the initial time part of
	 * a GUID.
	 */
	public StringIdentity(@Nonnull String name, boolean appendUUID) {
		this.name = checkNotNull(name)
			+ (appendUUID? ("-" + UUID.randomUUID().toString().substring(0,8)):"");
	}


	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof StringIdentity)) return false;

		StringIdentity that = (StringIdentity) o;

		if (!name.equals(that.name)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		return name;
	}
}
