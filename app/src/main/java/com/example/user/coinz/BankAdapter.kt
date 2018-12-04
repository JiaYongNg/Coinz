package com.example.user.coinz

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.layout_bank.view.*

class BankAdapter(private var coinList:ArrayList<BankActivity.CoinInfo>, private var selectedCoinList:ArrayList<BankActivity.CoinInfo>): RecyclerView.Adapter<CoinViewHolder>() {
    //create a view
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoinViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val cellForRow = layoutInflater.inflate(R.layout.layout_bank, parent, false)
        return CoinViewHolder(cellForRow)
    }

    override fun onBindViewHolder(holder: CoinViewHolder, position: Int) {
        val currentCoin = coinList[position]

        //acknowledge coin giver's name
        val displayText = if (coinList[position].coinGiverName != "") {
            currentCoin.value.toString() + currentCoin.currency + " given by " + currentCoin.coinGiverName

            //self collected coin
        } else {
            currentCoin.value.toString() + currentCoin.currency
        }
        holder.view.coin_text_view?.text = displayText



        //onClick the clicked coin would be highlighted and added to an arraylist
        holder.view.setOnClickListener {
            if(!selectedCoinList.contains(currentCoin)){
                //toggled
                //prevent adding the coin twice
                selectedCoinList.add(currentCoin)
                holder.view.coin_text_view.setBackgroundColor(Color.GREEN)



            }else{
                //not toggled
                selectedCoinList.remove(currentCoin)
                holder.view.coin_text_view.setBackgroundColor(Color.WHITE)
            }
            Log.d("Bank Activity","coin selected = ${selectedCoinList.size}")
            Log.d("BankActivity", "selected " + coinList[position].value.toString() + currentCoin.currency)
        }
        if(selectedCoinList.contains(currentCoin)){
            holder.view.coin_text_view.setBackgroundColor(Color.GREEN)
        }else{
            holder.view.coin_text_view.setBackgroundColor(Color.WHITE)
        }
    }


    // numberOfItems
    override fun getItemCount(): Int {
        return coinList.size
    }


}
class CoinViewHolder(val view: View): RecyclerView.ViewHolder(view)


