package code.transactionrequests

import code.model._
import code.transactionrequests.TransactionRequests._

import code.util.DefaultStringField
import net.liftweb.common.Logger
import net.liftweb.json
import net.liftweb.mapper._
import java.util.Date

object MappedTransactionRequestProvider extends TransactionRequestProvider {

  override protected def getTransactionRequestFromProvider(transactionRequestId: TransactionRequestId): Option[TransactionRequest] =
    MappedTransactionRequest.find(By(MappedTransactionRequest.mTransactionRequestId, transactionRequestId.value)).flatMap(_.toTransactionRequest)

  override protected def getTransactionRequestsFromProvider(bankId: BankId, accountId: AccountId, viewId: ViewId): Some[List[TransactionRequest]] = {
    Some(MappedTransactionRequest.findAll(By(MappedTransactionRequest.mTo_BankId, bankId.value), By(MappedTransactionRequest.mTo_AccountId, accountId.value)).flatMap(_.toTransactionRequest))
  }
}

class MappedTransactionRequest extends LongKeyedMapper[MappedTransactionRequest] with IdPK with CreatedUpdated {

  private val logger = Logger(classOf[MappedTransactionRequest])

  override def getSingleton = MappedTransactionRequest

  //transaction request fields:
  object mTransactionRequestId extends DefaultStringField(this)
  object mType extends DefaultStringField(this)

  //transaction fields:
  object mTransactionIDs extends DefaultStringField(this)
  object mStatus extends DefaultStringField(this)
  object mStartDate extends MappedDate(this)
  object mEndDate extends MappedDate(this)
  object mChallenge_Id extends DefaultStringField(this)
  object mChallenge_AllowedAttempts extends MappedInt(this)
  object mChallenge_ChallengeType extends DefaultStringField(this)
  object mCharge_Summary  extends DefaultStringField(this)
  object mCharge_Amount  extends DefaultStringField(this)
  object mCharge_Currency  extends DefaultStringField(this)
  object mcharge_Policy  extends DefaultStringField(this)

  //Body from http request: SANDBOX_TAN, FREE_FORM, SEPA and COUNTERPARTY should have the same following fields:
  object mBody_Value_Currency extends DefaultStringField(this)
  object mBody_Value_Amount extends DefaultStringField(this)
  object mBody_Description extends DefaultStringField(this)
  // This is the details / body of the request (contains all fields in the body)
  // Note:this need to be a longer string, defaults is 2000, maybe not enough
  object mDetails extends DefaultStringField(this)

  //fromAccount fields
  object mFrom_BankId extends DefaultStringField(this)
  object mFrom_AccountId extends DefaultStringField(this)

  //toAccount fields
  object mTo_BankId extends DefaultStringField(this)
  object mTo_AccountId extends DefaultStringField(this)

  //toCounterparty fields
  object mName extends DefaultStringField(this)
  object mThisBankId extends DefaultStringField(this)
  object mThisAccountId extends DefaultStringField(this)
  object mThisViewId extends DefaultStringField(this)
  object mCounterpartyId extends DefaultStringField(this)
  object mOtherAccountRoutingScheme extends DefaultStringField(this)
  object mOtherAccountRoutingAddress extends DefaultStringField(this)
  object mOtherBankRoutingScheme extends DefaultStringField(this)
  object mOtherBankRoutingAddress extends DefaultStringField(this)
  object mIsBeneficiary extends MappedBoolean(this)

  def updateStatus(newStatus: String) = {
    mStatus.set(newStatus)
  }

  def toTransactionRequest : Option[TransactionRequest] = {
    val t_amount = AmountOfMoney (
      currency = mBody_Value_Currency.get,
      amount = mBody_Value_Amount.get
    )
    val t_to = TransactionRequestAccount (
      bank_id = mTo_BankId.get,
      account_id = mTo_AccountId.get
    )
    val t_body = TransactionRequestBody (
      to = t_to,
      value = t_amount,
      description = mBody_Description.get
    )
    val t_from = TransactionRequestAccount (
      bank_id = mFrom_BankId.get,
      account_id = mFrom_AccountId.get
    )

    val t_challenge = TransactionRequestChallenge (
      id = mChallenge_Id,
      allowed_attempts = mChallenge_AllowedAttempts,
      challenge_type = mChallenge_ChallengeType
    )

    val t_charge = TransactionRequestCharge (
    summary = mCharge_Summary,
    value = AmountOfMoney(currency = mCharge_Currency, amount = mCharge_Amount)
    )


    val details = mDetails.get

    val parsedDetails = json.parse(details)


    Some(
      TransactionRequest(
        id = TransactionRequestId(mTransactionRequestId.get),
        `type`= mType.get,
        from = t_from,
        details = parsedDetails,
        body = t_body,
        status = mStatus.get,
        transaction_ids = mTransactionIDs.get,
        start_date = mStartDate.get,
        end_date = mEndDate.get,
        challenge = t_challenge,
        charge = t_charge,
        charge_policy =mcharge_Policy,
        counterparty_id =  CounterpartyId(mCounterpartyId.get),
        name = mName.get,
        this_bank_id = BankId(mThisBankId.get),
        this_account_id = AccountId(mThisAccountId.get),
        this_view_id = ViewId(mThisViewId.get),
        other_account_routing_scheme = mOtherAccountRoutingScheme.get,
        other_account_routing_address = mOtherAccountRoutingAddress.get,
        other_bank_routing_scheme = mOtherBankRoutingScheme.get,
        other_bank_routing_address = mOtherBankRoutingAddress.get,
        is_beneficiary = mIsBeneficiary.get
      )
    )
  }
}

object MappedTransactionRequest extends MappedTransactionRequest with LongKeyedMetaMapper[MappedTransactionRequest] {
  override def dbIndexes = UniqueIndex(mTransactionRequestId) :: super.dbIndexes
}
