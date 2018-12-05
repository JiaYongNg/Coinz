package com.example.user.coinz

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.layout_bank.view.*
import java.text.DecimalFormat

class BankAdapter(private var coinList:ArrayList<BankActivity.CoinInfo>, private var selectedCoinList:ArrayList<BankActivity.CoinInfo>): RecyclerView.Adapter<CoinViewHolder>() {
    //round up the UI displayed numbers to 3 decimal places
    private val df = DecimalFormat("#.###")

    //create a view
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoinViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val cellForRow = layoutInflater.inflate(R.layout.layout_bank, parent, false)
        return CoinViewHolder(cellForRow)
    }
    override fun onBindViewHolder(holder: CoinViewHolder, position: Int) {
        val currentCoin = coinList[position]

        val displayText =  df.format(currentCoin.value).toString() +  " " + currentCoin.currency +
                if (coinList[position].coinGiverName != "") {

                    //acknowledge coin giver's name
                    " given by " + currentCoin.coinGiverName

                }else{
                    //no acknowledgement
                    ""
                }
        holder.view.coin_text_view?.text = displayText



        //onClick the clicked coin would be highlighted and added to an arraylist
        holder.view.setOnClickListener {
            if(!selectedCoinList.contains(currentCoin)){
                //prevent adding the coin twice
                selectedCoinList.add(currentCoin)
                holder.view.coin_text_view.setBackgroundColor(Color.GREEN)

            }else{
                selectedCoinList.remove(currentCoin)
                holder.view.coin_text_view.setBackgroundColor(Color.WHITE)
            }
            Log.d("Bank Activity","coin selected = ${selectedCoinList.size}")
            Log.d("BankActivity", "selected " + coinList[position].value.toString() + currentCoin.currency)
        }
        //by default, colour highlights are as follow
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


