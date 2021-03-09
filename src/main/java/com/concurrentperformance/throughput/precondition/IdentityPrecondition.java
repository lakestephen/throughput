package com.concurrentperformance.throughput.precondition;

import com.concurrentperformance.throughput.identity.Identity;
import com.concurrentperformance.throughput.identity.StringIdentity;
import com.google.common.base.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * TODO SJL Comment
 *
 * @author Steve Lake
 */
public class IdentityPrecondition {
	public static final String VALID_IDENTITY_CHARACTERS_REGEX = "^[a-zA-Z0-9]+([\\.-][a-zA-Z0-9]+)*$";

	public static Identity checkIdentity(@Nonnull Identity identity, @Nullable String errorMessage) {
		if (identity instanceof StringIdentity) {
			return checkStringIdentity((StringIdentity)identity, errorMessage);
		}
		else {
			throw new IllegalArgumentException(" [" + identity + "] is an illegal identity type");
		}
	}

	public static StringIdentity checkStringIdentity(@Nonnull StringIdentity identity, @Nullable String errorMessage) {
		if (!identity.getName().matches(VALID_IDENTITY_CHARACTERS_REGEX)) {

			String msg = Strings.nullToEmpty(errorMessage) +
					" [" + identity + "] is an illegal identity name. It must match \'" + VALID_IDENTITY_CHARACTERS_REGEX + "\'";
			throw new IllegalArgumentException(msg);
		}
		return identity;
	}
}
