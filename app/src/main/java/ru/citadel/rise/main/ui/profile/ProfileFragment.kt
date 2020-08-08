package ru.citadel.rise.main.ui.profile

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import ru.avangard.rise.R
import ru.avangard.rise.databinding.FragmentProfileBinding
import ru.citadel.rise.main.MainActivity


/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentProfileBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)

        binding.butSettings.setOnClickListener { (activity as MainActivity).showSettingsFragment() }
        binding.butFavourites.setOnClickListener { (activity as MainActivity).showProjectsFragment(2) }
        binding.butAbout.setOnClickListener { (activity as MainActivity).showAboutAppFragment() }

        binding.butMyProjects.setOnClickListener { (activity as MainActivity).showAboutMeFragment() }

        val user = (activity as MainActivity).currentUser

        val text: Spannable = SpannableString(resources.getString(R.string.profile_phrase) + " " + user.name + "!")
        text.setSpan(StyleSpan(Typeface.BOLD), 18, text.length-1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.profileName.text =  text

        when(user.type){
            1 -> binding.profileImage.setImageResource(R.drawable.im_robot_with_idea)
            else -> binding.profileImage.setImageResource(R.drawable.im_robot_manager)
        }

        return binding.root
    }
}