package it.trade.android.sdk.model

import android.os.Parcel
import android.os.Parcelable

import it.trade.android.sdk.enums.TradeItOrderAction
import it.trade.android.sdk.enums.TradeItOrderExpirationType
import it.trade.android.sdk.enums.TradeItOrderPriceType
import it.trade.android.sdk.enums.TradeItOrderQuantityType
import it.trade.model.TradeItErrorResult
import it.trade.model.callback.TradeItCallback
import it.trade.model.reponse.TradeItPlaceStockOrEtfOrderResponse
import it.trade.model.reponse.TradeItPreviewStockOrEtfOrderResponse
import it.trade.model.request.TradeItPreviewStockOrEtfOrderRequest


class TradeItOrderParcelable : Parcelable {

    var linkedBrokerAccount: TradeItLinkedBrokerAccountParcelable? = null
        private set
    var symbol: String? = null
    var quantity: Double? = 1.0
    var limitPrice: Double? = null
    var stopPrice: Double? = null
    var quoteLastPrice: Double? = null
    var action: TradeItOrderAction = TradeItOrderAction.BUY
    var priceType: TradeItOrderPriceType = TradeItOrderPriceType.MARKET
        set(value) {
            field = value
            if (!requiresExpiration()) {
                expiration = TradeItOrderExpirationType.GOOD_FOR_DAY
            }
            if (!requiresLimitPrice()) {
                limitPrice = null
            }
            if (!requiresStopPrice()) {
                stopPrice = null
            }
        }
    var expiration: TradeItOrderExpirationType = TradeItOrderExpirationType.GOOD_FOR_DAY
    var isUserDisabledMargin = false
    var orderQuantityType: TradeItOrderQuantityType = TradeItOrderQuantityType.SHARES

    constructor(linkedBrokerAccount: TradeItLinkedBrokerAccountParcelable, symbol: String) {
        this.linkedBrokerAccount = linkedBrokerAccount
        this.symbol = symbol
    }

    fun requiresLimitPrice(): Boolean {
        return arrayListOf(
            TradeItOrderPriceType.LIMIT,
            TradeItOrderPriceType.STOP_LIMIT
        ).contains(priceType)
    }

    fun requiresStopPrice(): Boolean {
        return arrayListOf(
            TradeItOrderPriceType.STOP_MARKET,
            TradeItOrderPriceType.STOP_LIMIT
        ).contains(priceType)
    }

    fun requiresExpiration(): Boolean {
        return arrayListOf(
            TradeItOrderPriceType.LIMIT,
            TradeItOrderPriceType.STOP_MARKET,
            TradeItOrderPriceType.STOP_LIMIT
        ).contains(priceType)
    }

    fun previewOrder(callback: TradeItCallback<TradeItPreviewStockOrEtfOrderResponseParcelable>) {
        val previewRequest = TradeItPreviewStockOrEtfOrderRequest(
                this.linkedBrokerAccount!!.accountNumber,
                this.action.actionValue,
                if (this.quantity != null) this.quantity!!.toString() else "1",
                this.symbol,
                this.priceType.priceTypeValue,
                if (this.limitPrice != null) this.limitPrice!!.toString() else null,
                if (this.stopPrice != null) this.stopPrice!!.toString() else null,
                this.expiration.expirationValue,
                this.orderQuantityType.value,
                this.isUserDisabledMargin
        )

        val order = this

        this.linkedBrokerAccount!!.tradeItApiClient!!.previewStockOrEtfOrder(
                previewRequest,
                object : TradeItCallback<TradeItPreviewStockOrEtfOrderResponse> {
                    override fun onSuccess(response: TradeItPreviewStockOrEtfOrderResponse) {
                        callback.onSuccess(TradeItPreviewStockOrEtfOrderResponseParcelable(response))
                    }

                    override fun onError(error: TradeItErrorResult) {
                        val errorResultParcelable = TradeItErrorResultParcelable(error)
                        order.linkedBrokerAccount!!.setErrorOnLinkedBroker(errorResultParcelable)
                        callback.onError(errorResultParcelable)
                    }
                }
        )
    }

    fun placeOrder(orderId: String, callback: TradeItCallback<TradeItPlaceStockOrEtfOrderResponseParcelable>) {
        this.linkedBrokerAccount!!.tradeItApiClient!!.placeStockOrEtfOrder(orderId, object : TradeItCallback<TradeItPlaceStockOrEtfOrderResponse> {
            override fun onSuccess(response: TradeItPlaceStockOrEtfOrderResponse) {
                callback.onSuccess(TradeItPlaceStockOrEtfOrderResponseParcelable(response))
            }

            override fun onError(error: TradeItErrorResult) {
                val errorResultParcelable = TradeItErrorResultParcelable(error)
                callback.onError(errorResultParcelable)
            }
        })
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(this.linkedBrokerAccount, flags)
        dest.writeString(this.symbol)
        dest.writeValue(this.quantity)
        dest.writeValue(this.limitPrice)
        dest.writeValue(this.stopPrice)
        dest.writeValue(this.quoteLastPrice)
        dest.writeInt(this.action.ordinal)
        dest.writeInt(this.priceType.ordinal)
        dest.writeInt(this.expiration.ordinal)
        dest.writeByte((if (isUserDisabledMargin) 1 else 0).toByte())
        dest.writeInt(this.orderQuantityType.ordinal)
    }

    protected constructor(`in`: Parcel) {
        this.linkedBrokerAccount = `in`.readParcelable(TradeItLinkedBrokerAccountParcelable::class.java.getClassLoader())
        this.symbol = `in`.readString()
        this.quantity = `in`.readValue(Double::class.java.getClassLoader()) as? Double
        this.limitPrice = `in`.readValue(Double::class.java.getClassLoader()) as? Double
        this.stopPrice = `in`.readValue(Double::class.java.getClassLoader()) as? Double
        this.quoteLastPrice = `in`.readValue(Double::class.java.getClassLoader()) as? Double
        val tmpAction = `in`.readInt()
        this.action = TradeItOrderAction.values()[tmpAction]
        val tmpPriceType = `in`.readInt()
        this.priceType = TradeItOrderPriceType.values()[tmpPriceType]
        val tmpExpiration = `in`.readInt()
        this.expiration = TradeItOrderExpirationType.values()[tmpExpiration]
        this.isUserDisabledMargin = `in`.readByte().toInt() != 0
        this.orderQuantityType = TradeItOrderQuantityType.values()[`in`.readInt()]
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<TradeItOrderParcelable> = object : Parcelable.Creator<TradeItOrderParcelable> {
            override fun createFromParcel(source: Parcel): TradeItOrderParcelable {
                return TradeItOrderParcelable(source)
            }

            override fun newArray(size: Int): Array<TradeItOrderParcelable?> {
                return arrayOfNulls(size)
            }
        }
    }
}
