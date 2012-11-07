package grails.plugins.crm.security

/**
 * A feature or service included in a CRM account.
 */
class CrmAccountItem {
    Integer quantity
    String productId

    static belongsTo = [account: CrmAccount]
    static constraints = {
        quantity(min: -99999, max: 99999)
        productId(maxSize: 40, blank: false, unique: 'account')
    }
    static mapping = {
        cache true
    }

    String toString() {
        "$quantity $productId"
    }
}
