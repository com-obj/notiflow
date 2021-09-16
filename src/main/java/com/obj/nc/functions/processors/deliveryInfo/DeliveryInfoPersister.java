/*
 *   Copyright (C) 2021 the original author or authors.
 *
 *   This file is part of Notiflow
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.obj.nc.functions.processors.deliveryInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;
import com.obj.nc.repositories.DeliveryInfoRepository;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class DeliveryInfoPersister extends ProcessorFunctionAdapter<List<DeliveryInfo>,List<DeliveryInfo>> {

    @Autowired
    private DeliveryInfoRepository deliveryInfoRepo;

	@Override
	protected List<DeliveryInfo> execute(List<DeliveryInfo> deliveryInfos) {
		List<DeliveryInfo> deliveryInfosInDB = new ArrayList<>();
		
		
		deliveryInfos.forEach(deliveryInfo -> {
			if (deliveryInfo.isNew()) {
				deliveryInfo.setId(UUID.randomUUID());
			}
			
			DeliveryInfo deliveryInfoInDB = deliveryInfoRepo.save(deliveryInfo);
			deliveryInfosInDB.add(deliveryInfoInDB);
		});

		
		return deliveryInfosInDB;
	}	

}
