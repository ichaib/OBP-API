package code.customer

import java.util.Date

import code.model.{BankId, User}
import code.remotedata.RemotedataCustomers
import net.liftweb.common.Box
import net.liftweb.util.SimpleInjector

object Customer extends SimpleInjector {

  val customerProvider = new Inject(buildOne _) {}

  //def buildOne: CustomerProvider = MappedCustomerProvider
  def buildOne: CustomerProvider = RemotedataCustomers

}

trait CustomerProvider {
  def getCustomerByResourceUserId(bankId: BankId, resourceUserId: Long): Box[Customer]

  def getCustomerByCustomerId(customerId: String): Box[Customer]

  def getBankIdByCustomerId(customerId: String): Box[String]

  def getCustomerByCustomerNumber(customerNumber: String, bankId : BankId): Box[Customer]

  def getUser(bankId : BankId, customerNumber : String) : Box[User]

  def checkCustomerNumberAvailable(bankId : BankId, customerNumber : String) : Boolean


  def addCustomer(bankId: BankId, user: User, number: String, legalName: String, mobileNumber: String, email: String, faceImage: CustomerFaceImage,
                  dateOfBirth: Date,
                  relationshipStatus: String,
                  dependents: Int,
                  dobOfDependents: List[Date],
                  highestEducationAttained: String,
                  employmentStatus: String,
                  kycStatus: Boolean,
                  lastOkDate: Date,
                  creditRating: Option[CreditRating],
                  creditLimit: Option[AmountOfMoney]
                 ): Box[Customer]

  def bulkDeleteCustomers(): Boolean

}

class RemotedataCustomerProviderCaseClasses {
  case class getCustomerByResourceUserId(bankId: BankId, resourceUserId: Long)
  case class getCustomerByCustomerId(customerId: String)
  case class getBankIdByCustomerId(customerId: String)
  case class getCustomerByCustomerNumber(customerNumber: String, bankId : BankId)
  case class getUser(bankId : BankId, customerNumber : String)
  case class checkCustomerNumberAvailable(bankId : BankId, customerNumber : String)
  case class addCustomer(bankId: BankId, user: User, number: String, legalName: String, mobileNumber: String, email: String, faceImage: CustomerFaceImage,
                         dateOfBirth: Date,
                         relationshipStatus: String,
                         dependents: Int,
                         dobOfDependents: List[Date],
                         highestEducationAttained: String,
                         employmentStatus: String,
                         kycStatus: Boolean,
                         lastOkDate: Date,
                         creditRating: Option[CreditRating],
                         creditLimit: Option[AmountOfMoney]
                        )
  case class bulkDeleteCustomers()

}

object RemotedataCustomerProviderCaseClasses extends RemotedataCustomerProviderCaseClasses

trait Customer {
  def customerId : String // The UUID for the customer. To be used in URLs
  def bank : String
  def number : String // The Customer number i.e. the bank identifier for the customer.
  def legalName : String
  def mobileNumber : String
  def email : String
  def faceImage : CustomerFaceImage
  def dateOfBirth: Date
  def relationshipStatus: String
  def dependents: Int
  def dobOfDependents: List[Date]
  def highestEducationAttained: String
  def employmentStatus: String
  def creditRating : CreditRating
  def creditLimit: AmountOfMoney
  def kycStatus: Boolean
  def lastOkDate: Date
}

trait CustomerFaceImage {
  def url : String
  def date : Date
}

trait AmountOfMoney {
  def currency: String
  def amount: String
}

trait CreditRating {
  def rating: String
  def source: String
}

case class MockCustomerFaceImage(date : Date, url : String) extends CustomerFaceImage
case class MockCreditRating(rating: String, source: String) extends CreditRating
case class MockCreditLimit(currency: String, amount: String) extends AmountOfMoney