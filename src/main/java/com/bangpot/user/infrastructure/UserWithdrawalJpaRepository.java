package com.bangpot.user.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

interface UserWithdrawalJpaRepository extends JpaRepository<UserWithdrawalJpaEntity, Long> {
}
