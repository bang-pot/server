package com.banglog.explore.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.banglog.explore.domain.Store;

interface StoreJpaRepository extends JpaRepository<Store, Long> {

	@Query("""
		select distinct s.region as region, s.district as district
		from Theme t, Store s
		where s.id = t.storeId
		  and t.active = true
		  and s.region is not null
		order by s.region asc, s.district asc
		""")
	List<RegionDistrictProjection> findActiveRegionDistricts();

	interface RegionDistrictProjection {
		String getRegion();

		String getDistrict();
	}
}
