/**
 * Copyright © 2017 Ties BV
 *
 * This file is part of Ties.DB project.
 *
 * Ties.DB project is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Ties.DB project is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with Ties.DB project. If not, see <https://www.gnu.org/licenses/lgpl-3.0>.
 */
package network.tiesdb.service.api;

import network.tiesdb.api.TiesVersion;
import network.tiesdb.service.scope.api.TiesServiceScope;

/**
 * TiesDB service API.
 * 
 * <P>
 * Defines common service functions.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
public interface TiesService {

    TiesServiceDaemon getDaemon();

    TiesVersion getVersion();

    TiesServiceScope newServiceScope();

}
