package com.bangpot.user.domain.view;

import java.util.List;

public record MyWithdrawalCheckView(
	boolean canWithdraw,
	List<MyWithdrawalCheckView.BlockingActiveCrew> blockingActiveCrews
) {
	public static MyWithdrawalCheckView of(
		boolean canWithdraw,
		List<MyWithdrawalCheckView.BlockingActiveCrew> blockingActiveCrews
	) {
		return new MyWithdrawalCheckView(canWithdraw, blockingActiveCrews);
	}

	public record BlockingActiveCrew(
		Long crewId,
		String crewName
	) {
		public static BlockingActiveCrew of(Long crewId, String crewName) {
			return new BlockingActiveCrew(crewId, crewName);
		}
	}
}
