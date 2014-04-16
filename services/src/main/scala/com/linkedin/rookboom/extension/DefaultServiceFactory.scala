/*
 * (c) Copyright 2014 LinkedIn Corp. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.linkedin.rookboom.extension

import com.linkedin.rookboom.user.{LdapUserManager, UserManager}
import com.linkedin.rookboom.schedule._
import com.linkedin.rookboom.layout.{LdapLayoutManager, LayoutManager}
import com.linkedin.rookboom.filter.{FileFilterManager, FilterManager}
import java.io.File
import org.springframework.ldap.core.LdapTemplate
import com.linkedin.rookboom.schedule.dao.{EwsAppointmentDao, EwsScheduleDao, InternalScheduleDao}
import net.sf.ehcache.CacheManager
import org.springframework.transaction.PlatformTransactionManager
import com.linkedin.rookboom.util.Logging

/**
 * Default service factory implementation. It can be subclassed to override only certain methods.
 * @author Dmitriy Yefremov
 */
class DefaultServiceFactory(context: ServiceContext) extends AbstractServiceFactory(context) with Logging {

  /**
   * All interface methods are implemented as lazy vals because of 2 reasons:
   * 1) they are vals because they reference each other and defs would create multiple instances
   * 2) they are lazy because instances should not be created if they are overridden in a sub class
   */

  lazy val filterManager: FilterManager = {
    log.info("Creating FileFilterManager")
    val filtersFile = getConfigFile("filters.json")
    val filterManager = new FileFilterManager(filtersFile)
    val reloadTrigger = context.getProperty("filter.reload.cron")
    context.schedule(reloadTrigger, filterManager.reload(), init = true)
    filterManager
  }

  lazy val timeMaskManager: TimeMaskManager = {
    log.info("Creating TimeMaskManager")
    new TimeMaskManagerImpl()
  }

  lazy val layoutManager: LayoutManager = {
    log.info("Creating LdapLayoutManager")
    val ldapTemplate = context.getDependency(classOf[LdapTemplate])
    val searchBase = context.getProperty("ldap.rooms.base")
    val searchFilter = context.getProperty("ldap.rooms.filter")
    val layoutManager = new LdapLayoutManager(ldapTemplate, searchBase, searchFilter)
    val reloadTrigger = context.getProperty("layout.reload.cron")
    context.schedule(reloadTrigger, layoutManager.reload(), init = true)
    layoutManager
  }

  lazy val userManager: UserManager = {
    log.info("Creating LdapUserManager")
    val ldapTemplate = context.getDependency(classOf[LdapTemplate])
    val usersBase = context.getProperty("ldap.users.base")
    val usersFilter = context.getProperty("ldap.users.filter")
    val groupsBase = context.getProperty("ldap.groups.base")
    val groupsFilter = context.getProperty("ldap.groups.filter")
    val userManager = new LdapUserManager(ldapTemplate, usersBase, usersFilter, groupsBase, groupsFilter)
    val reloadTrigger = context.getProperty("users.reload.cron")
    context.schedule(reloadTrigger, userManager.reload(), init = true)
    userManager
  }

  lazy val ewsScheduleManager = {
    log.info("Creating EwsScheduleManager")
    val txManager = context.getDependency(classOf[PlatformTransactionManager])
    val internalDao = context.getDependency(classOf[InternalScheduleDao])
    val ewsDao = context.getDependency(classOf[EwsScheduleDao])
    val reloadDays = context.getProperty("exchange.reload.days").toInt
    val resolveDays = context.getProperty("exchange.resolve.days").toInt
    val ewsScheduleManager = new EwsScheduleManager(txManager, userManager, layoutManager, internalDao, ewsDao, reloadDays, resolveDays)
    val reloadTrigger = context.getProperty("exchange.reload.cron")
    context.schedule(reloadTrigger, ewsScheduleManager.reload())
    val resolveTrigger = context.getProperty("exchange.resolve.cron")
    context.schedule(resolveTrigger, ewsScheduleManager.resolve())
    ewsScheduleManager
  }

  lazy val cachedScheduleManager = {
    log.info("Creating CachedScheduleManager")
    val cacheManager = context.getDependency(classOf[CacheManager])
    new CachedScheduleManager(ewsScheduleManager, cacheManager)
  }

  lazy val scheduleManager: ScheduleManager = cachedScheduleManager

  lazy val bookingService: BookingService = {
    log.info("Creating BookingServiceImpl")
    val txManager = context.getDependency(classOf[PlatformTransactionManager])
    val appointmentDao = context.getDependency(classOf[EwsAppointmentDao])
    val internalDao = context.getDependency(classOf[InternalScheduleDao])
    val timeout = context.getProperty("booking.timeout.millis").toLong
    val eventListeners = Seq(cachedScheduleManager, ewsScheduleManager)
    new BookingServiceImpl(txManager, appointmentDao, internalDao, ewsScheduleManager, eventListeners, timeout, timeout)
  }

  def getConfigFile(fileName: String): File = {
    val fullPath = context.getProperty("rookboom.config.dir") + File.separator + fileName
    new File(fullPath)
  }

}
