package grails.plugins.crm.security

class CrmUserCleanerJob {
    static triggers = {
        cron name: 'crmUserCleaner', cronExpression: "0 5 0 * * ?" // every day 5 minutes after midnight.
    }

    def group = 'crm-account'

    def crmSecurityService

    def execute() {
        def result = CrmUser.createCriteria().list() {
            projections {
                property 'username'
            }
            eq 'status', CrmUser.STATUS_NEW
            lt 'dateCreated', new Date() - 2 // 48 hours ago.
        }
        for(username in result) {
            crmSecurityService.deleteUser(username)
        }
    }
}