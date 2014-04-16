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

package com.linkedin.rookboom.util

import org.springframework.transaction.support.DefaultTransactionDefinition
import scala.util.control.NonFatal
import org.springframework.transaction.PlatformTransactionManager

/**
 * Programmatic transactions management helpers.
 * @author Dmitriy Yefremov
 */
object TxUtils extends Logging {

  /**
   * Executes the given block of code in the context of a transaction.
   */
  def transaction[T](readOnly: Boolean = false)(f: => T)(implicit txManager: PlatformTransactionManager): T = {
    val txDef = new DefaultTransactionDefinition()
    txDef.setReadOnly(readOnly)
    log.debug("Creating a new transaction")
    val txStatus = txManager.getTransaction(txDef)
    val result = try {
      f
    } catch {
      case NonFatal(e) => {
        log.debug("Rolling back the transaction")
        txManager.rollback(txStatus)
        throw e
      }
    }
    log.debug("Committing the transaction")
    txManager.commit(txStatus)
    result
  }

  def readOnlyTransaction[T](f: => T)(implicit txManager: PlatformTransactionManager): T = transaction(readOnly = true)(f)(txManager)

  def readWriteTransaction[T](f: => T)(implicit txManager: PlatformTransactionManager): T = transaction(readOnly = false)(f)(txManager)

}
