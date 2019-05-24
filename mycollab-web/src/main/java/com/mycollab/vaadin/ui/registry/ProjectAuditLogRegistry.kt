/**
 * Copyright © MyCollab
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mycollab.vaadin.ui.registry

import com.mycollab.module.project.ProjectTypeConstants
import com.mycollab.module.project.ui.format.*
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Component

/**
 * @author MyCollab Ltd
 * @since 6.0.0
 */
@Component
class ProjectAuditLogRegistry(private val auditLogRegistry: AuditLogRegistry) : InitializingBean {

    override fun afterPropertiesSet() {
        auditLogRegistry.registerAuditLogHandler(ProjectTypeConstants.PROJECT, ProjectFieldFormatter.instance())
        auditLogRegistry.registerAuditLogHandler(ProjectTypeConstants.BUG, BugFieldFormatter.instance())
        auditLogRegistry.registerAuditLogHandler(ProjectTypeConstants.TASK, TaskFieldFormatter.instance())
        auditLogRegistry.registerAuditLogHandler(ProjectTypeConstants.MILESTONE, MilestoneFieldFormatter.instance())
        auditLogRegistry.registerAuditLogHandler(ProjectTypeConstants.RISK, RiskFieldFormatter.instance())
        auditLogRegistry.registerAuditLogHandler(ProjectTypeConstants.COMPONENT, ComponentFieldFormatter.instance())
        auditLogRegistry.registerAuditLogHandler(ProjectTypeConstants.VERSION, VersionFieldFormatter.instance())
        auditLogRegistry.registerAuditLogHandler(ProjectTypeConstants.INVOICE, InvoiceFieldFormatter.instance())
    }
}