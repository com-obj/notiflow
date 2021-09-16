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

ALTER INDEX nc_intent_event_ids_idx RENAME TO nc_intent_previous_event_ids_idx;
CREATE INDEX nc_intent_previous_intent_ids_idx ON nc_intent USING GIN (previous_intent_ids);

ALTER INDEX nc_message_event_ids_idx RENAME TO nc_message_previous_event_ids_idx;
CREATE INDEX nc_message_previous_intent_ids_idx ON nc_message USING GIN (previous_intent_ids);
CREATE INDEX nc_message_previous_message_ids_idx ON nc_message USING GIN (previous_message_ids);

CREATE INDEX nc_delivery_info_intent_id_idx ON nc_delivery_info (intent_id);