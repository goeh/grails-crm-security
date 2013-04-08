package grails.plugins.crm.security

/**
 * A feature or service included in a CRM account.
 */
class CrmAccountItem {
    String productId
    Integer quantity
    String unit
    Float discount

    static belongsTo = [account: CrmAccount]
    static constraints = {
        productId(maxSize: 40, blank: false, unique: 'account')
        quantity(min: -99999, max: 99999)
        unit(maxSize: 40, nullable: false, blank: false)
        discount(nullable: true, min: -999999f, max: 999999f, scale: 2)
    }
    static mapping = {
        cache true
    }

    String toString() {
        "$quantity $unit $productId"
    }
}
