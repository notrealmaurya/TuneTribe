package com.maurya.dtxloopplayer.Fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.maurya.dtxloopplayer.R

class AboutDialogFragment : DialogFragment() {


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.about_dialog, null)

        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(view)

        val closeButton = view.findViewById<Button>(R.id.about_dialog_thankyou_button)
        closeButton.setOnClickListener {
            dismiss()
        }

        val textView = view.findViewById<TextView>(R.id.spannableTextView_Dialog)
        val spannableString =
            SpannableString("If you'd like to share your thoughts or provide Feedback , please feel free to do so. Your input is valuable, and I'd appreciate hearing from you.❤\uFE0F\"\n ")

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val websiteUrl =
                    "https://docs.google.com/forms/d/e/1FAIpQLSfRsCpO9jc0t61V6E5IkjH6L0HSoWmk2LQdy0EPJ1SmBL7_hQ/viewform"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl))
                startActivity(intent)
            }
        }

        spannableString.setSpan(
            clickableSpan,
            48, 56,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        val blueColor = Color.BLUE
        spannableString.setSpan(
            ForegroundColorSpan(blueColor),
            48, 56,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        textView.text = spannableString
        textView.movementMethod = LinkMovementMethod.getInstance()




        return builder.create()
    }

}