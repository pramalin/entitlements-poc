package com.alai.entitlements.repository;

import com.alai.entitlements.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<AppUser, Integer> {
}
