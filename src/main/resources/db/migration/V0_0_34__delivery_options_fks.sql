/*
 *   Copyright (C) 2021 the original author or authors.
 *
 *   This file is part of Notiflow
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

ALTER TABLE nc_delivery_info ADD CONSTRAINT fk_endpoint FOREIGN KEY (endpoint_id) REFERENCES nc_endpoint (id) on delete restrict;
ALTER TABLE nc_delivery_info ADD CONSTRAINT fk_message FOREIGN KEY (message_id) REFERENCES nc_message (id) on delete restrict;   
ALTER TABLE nc_delivery_info ADD CONSTRAINT fk_intent FOREIGN KEY (intent_id) REFERENCES nc_intent (id) on delete restrict;   
ALTER TABLE nc_delivery_info ADD CONSTRAINT fk_event FOREIGN KEY (event_id) REFERENCES nc_event (id) on delete restrict;   
