package com.business.visiting.card.creator.editor.ui.savedwork

import android.app.AlertDialog
import android.app.RecoverableSecurityException
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.business.visiting.card.creator.editor.databinding.FragmentMyWorkPdfBinding
import com.business.visiting.card.creator.editor.inappbilling.BusinessCardAppBilling
import com.business.visiting.card.creator.editor.ui.previewcard.TestPdfActivity
import com.business.visiting.card.creator.editor.utils.ConstantsUtil
import com.business.visiting.card.creator.editor.utils.RemoteHandle
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import java.io.File

class MyWorkPdfFragment : Fragment() {

    lateinit var binding: FragmentMyWorkPdfBinding
    internal lateinit var myList: MutableList<String>
    internal lateinit var file: File


    var filesList: MutableList<File> = mutableListOf()

    val myWorkPdfAdapter : MyWorkPdfAdapter by lazy {
        MyWorkPdfAdapter(filesList, onSingleClick =  {


            try {
                val pdffile = it
                val path = Uri.fromFile(pdffile)

                Log.d("test_file","Absolute path of card: ${pdffile.absolutePath}")


                ConstantsUtil.path=path

                val builder = StrictMode.VmPolicy.Builder()
                StrictMode.setVmPolicy(builder.build())
//                            val intent= Intent(requireActivity(),PreviewCardPdfActivity::class.java)
////                            intent.putExtra("pathFile",path)
//                            startActivity(intent)
                startActivity(Intent(TestPdfActivity.launchPdfFromPath(requireActivity(), pdffile.absolutePath, pdffile
                    .name, "", false)))

//                            val builder = StrictMode.VmPolicy.Builder()
//                            StrictMode.setVmPolicy(builder.build())
//                            val intent = Intent(Intent.ACTION_VIEW)
//                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
//                            intent.setDataAndType(path, "application/pdf")
//                            startActivity(intent)
            } catch (e: Exception) {

                Toast.makeText(requireContext(), "Something Went Wrong", Toast.LENGTH_SHORT).show()

            }

        }, onDeleteClick = {position,it->

            showDeleteDialog(requireActivity(),position,it)
        }, onShareClick = {
            val file = it
            val photoURI = FileProvider.getUriForFile(
                requireContext(),
                requireActivity().packageName + ".my.package.name.provider",
                file
            )

            val intentShareFile = Intent(Intent.ACTION_SEND)

            intentShareFile.type = "application/pdf"
            intentShareFile.putExtra(
                Intent.EXTRA_STREAM,
                photoURI
            )
            intentShareFile.putExtra(
                Intent.EXTRA_SUBJECT,
                "Sharing File..."
            )
            intentShareFile.putExtra(Intent.EXTRA_TEXT, "Sharing File...")
            startActivity(Intent.createChooser(intentShareFile, "Share File"))
        })
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding= FragmentMyWorkPdfBinding.inflate(layoutInflater,container,false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        inits()

    }

    private fun inits() {
        filesList.clear()
        showSavedPdfs()
    }

    private fun initViews() {

    }


    fun showDeleteDialog(context: Context,position: Int,file: File) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete Saved PDF Card")
        builder.setMessage("Are you sure to delete Card?")

        builder.setPositiveButton(android.R.string.yes) { dialog, which ->
            try {
                if(file.delete()){
                    removeFile(position,file)
                }
            }catch (e: Exception){
                Log.d("test_remove","exception occer ${e.message}")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (e is RecoverableSecurityException) {
                        val recoverableSecurityException = e as RecoverableSecurityException
                        val intentSender = recoverableSecurityException.userAction.actionIntent.intentSender
                        if (intentSender != null) {
                            try {
                                requireContext().startIntentSender(intentSender, null, 0, 0, 0)
                                if(file.delete()){
                                    removeFile(position,file)
                                }
                            } catch (sendIntentException: IntentSender.SendIntentException) {
//                                sendIntentException.printStackTrace()
                            } catch (e: Exception){}
                        }
                    }
                }
            }
        }

        builder.setNegativeButton(android.R.string.no) { dialog, which ->
        }
        builder.show()
    }

    private fun removeFile(position: Int,file: File) {
        myWorkPdfAdapter.removeItem(position)
        checkListContainsFiles()
    }

    private fun checkListContainsFiles(){
        if(filesList.size>0){
            binding.imgEmptyPdf.visibility=View.GONE
            binding.bannerMyWorkCardview.visibility=View.VISIBLE
        }else{
            binding.imgEmptyPdf.visibility=View.VISIBLE
            binding.bannerMyWorkCardview.visibility=View.GONE
        }
    }

    fun showSavedPdfs() {
        try {
            myList = ArrayList()
            var root: String = ""
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                root =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).path.toString() + "/PDF"
            } else {
                root = Environment.getExternalStorageDirectory().toString() + "/DCIM/Business Card/PDF"
            }
            //  root += File.separator.toString() + "resume"
            file = File(root)
            val list = file.listFiles()
            if(list!=null){
                list.forEach {
                    filesList.add(it)
                }

                Log.d("test_file","total files: ${filesList.size}")
                filesList.reverse()


                if(filesList.size>0){
                    checkBannerAd()
                }else{
                    binding.imgEmptyPdf.visibility=View.VISIBLE
                    binding.bannerMyWorkCardview.visibility=View.GONE
                }



                val linearLayout = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false);
                binding.recyclerViewMyPdf.layoutManager=linearLayout
                binding.recyclerViewMyPdf.adapter=myWorkPdfAdapter
            }else {

                binding.imgEmptyPdf.visibility=View.VISIBLE
                binding.bannerMyWorkCardview.visibility=View.GONE

            }

//            if (list != null) {
//                for (i in list.indices) {
//                    myList.add(list[i].name)
//                }
//                Collections.reverse(myList)
//
////                val adapter = ArrayAdapter<String>(
////                    requireContext(),
////                    R.layout.pdf_listview_item, R.id.textViewPdf, myList
////                )
////                binding.recyclerViewMyPdf.adapter = adapter
//
////                binding.recyclerViewMyPdf.onItemClickListener =
////                    AdapterView.OnItemClickListener { adapterView, view, i, l ->
////                        try {
////                            val pdffile = File(file.toString() + "/" + myList[i])
////                            val path = Uri.fromFile(pdffile)
////
////                            Log.d("test_file","Absolute path of card: ${pdffile.absolutePath}")
////
////
////                            ConstantsUtil.path=path
////
////                            val builder = StrictMode.VmPolicy.Builder()
////                            StrictMode.setVmPolicy(builder.build())
//////                            val intent= Intent(requireActivity(),PreviewCardPdfActivity::class.java)
////////                            intent.putExtra("pathFile",path)
//////                            startActivity(intent)
////                            startActivity(Intent(TestPdfActivity.launchPdfFromPath(requireActivity(), pdffile.absolutePath, pdffile
////                                .name, "", false)))
////
//////                            val builder = StrictMode.VmPolicy.Builder()
//////                            StrictMode.setVmPolicy(builder.build())
//////                            val intent = Intent(Intent.ACTION_VIEW)
//////                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
//////                            intent.setDataAndType(path, "application/pdf")
//////                            startActivity(intent)
////                        } catch (e: Exception) {
////
////                            Toast.makeText(requireContext(), "Something Went Wrong", Toast.LENGTH_SHORT).show()
////
////                        }
////                    }
//
//
////                binding.recyclerViewMyPdf.onItemLongClickListener =
////                    AdapterView.OnItemLongClickListener { adapterView, view, i, l ->
////
////                        Toast.makeText(requireContext(), "Share", Toast.LENGTH_SHORT).show()
////                        view.findViewById<TextView>(R.id.textViewPdf)
////                            .setBackgroundColor(Color.parseColor("#9B9FA1"))
////
////                        Log.d("ssccdds", adapterView.getChildCount().toString())
////                        Handler().postDelayed({
////                            view.findViewById<TextView>(R.id.textViewPdf)
////                                .setBackgroundColor(Color.parseColor("#E1E3E4"))
////                        }, 3000)
////
////
////                        val file = File(file.toString() + "/" + myList[i])
////                        val photoURI = FileProvider.getUriForFile(
////                            requireContext(),
////                            requireActivity().packageName + ".my.package.name.provider",
////                            file
////                        )
////
////                        val intentShareFile = Intent(Intent.ACTION_SEND)
////
////                        intentShareFile.type = "application/pdf"
////                        intentShareFile.putExtra(
////                            Intent.EXTRA_STREAM,
////                            photoURI
////                        )
////                        intentShareFile.putExtra(
////                            Intent.EXTRA_SUBJECT,
////                            "Sharing File..."
////                        )
////                        intentShareFile.putExtra(Intent.EXTRA_TEXT, "Sharing File...")
////                        startActivity(Intent.createChooser(intentShareFile, "Share File"))
////                        true
////                    }
//
//
//
//
//                try {
//                    if (list.size > 0) {
//
//                        binding.imgEmptyPdf.visibility=View.GONE
//                        checkBannerAd()
//
//
//                    } else {
//
//                        binding.bannerMyWorkCardview.visibility=View.GONE
//                        binding.imgEmptyPdf.visibility=View.VISIBLE
//                    }
//                } catch (e: Exception) {
//                }
//
//
//            } else {
//
//                binding.imgEmptyPdf.visibility=View.VISIBLE
//                binding.bannerMyWorkCardview.visibility=View.GONE
//
//            }
        } catch (e: Exception) {



        }

    }


    private fun checkBannerAd() {
        if(!BusinessCardAppBilling.isPurchase){
            binding.bannerMyWorkCardview.visibility= View.VISIBLE
            if (1 == RemoteHandle.ADMOB_MY_WORK_BANNER && "" != RemoteHandle.Admob_BANNER_ID) {
                binding.bannerMywork.post {
                    loadBannerAd(RemoteHandle.Admob_BANNER_ID)
                }
            }else{
                binding.bannerMyWorkCardview.visibility=View.GONE
            }
        }else{
            binding.bannerMyWorkCardview.visibility=View.GONE
        }
    }

    private fun loadBannerAd(admobBannerId: String) {
        when (RemoteHandle.ADMOB_BANNER_STYLE_MY_WORK) {
            0 -> {
                binding.bannerMywork.visibility = View.GONE
            }

            1 -> {
                var adView = AdView(requireContext())
                adView?.adUnitId = admobBannerId
                binding.bannerMywork.removeAllViews()
                binding.bannerMywork.addView(adView)
                val adSize = getAdSize()
                adView?.setAdSize(adSize)
                var adRequest = AdRequest.Builder().build()
                adView.loadAd(adRequest)
                adView?.setAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.d("onAdFailedToLoad", loadAdError.toString())
                    }

                    override fun onAdLoaded() {
//                rel_banner?.setBackgroundColor(Color.TRANSPARENT)
                    }
                })
            }

            2 -> {
                // test collapse banner
                val extras = Bundle()
                extras.putString("collapsible", "top")

                var adView = AdView(requireContext())
//                adView.adUnitId = "ca-app-pub-3940256099942544/2014213617"
                adView.adUnitId = admobBannerId
                binding.bannerMywork.removeAllViews()
                binding.bannerMywork.addView(adView)
                val adSize = getAdSize()
                adView?.setAdSize(adSize)
                var adRequest = AdRequest.Builder()
                    .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
                    .build()
                adView.loadAd(adRequest)
                adView?.setAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.d("onAdFailedToLoad", loadAdError.toString())
                    }

                    override fun onAdLoaded() {
//                rel_banner?.setBackgroundColor(Color.TRANSPARENT)
                    }
                })
            }

            3 -> {
                // test collapse banner
                val extras = Bundle()
                extras.putString("collapsible", "bottom")

                var adView = AdView(requireContext())
                adView.adUnitId = admobBannerId
                binding.bannerMywork.removeAllViews()
                binding.bannerMywork.addView(adView)
                val adSize = getAdSize()
                adView?.setAdSize(adSize)
                var adRequest = AdRequest.Builder()
                    .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
                    .build()
                adView.loadAd(adRequest)
                adView?.setAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.d("onAdFailedToLoad", loadAdError.toString())
                    }

                    override fun onAdLoaded() {
//                rel_banner?.setBackgroundColor(Color.TRANSPARENT)
                    }
                })
            }
        }
    }

    private fun getAdSize(): AdSize {
        val display: Display = requireActivity().getWindowManager().getDefaultDisplay()
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)
        val density = outMetrics.density
        var adWidthPixels = binding.bannerMywork.width.toFloat()
        // If the ad hasn't been laid out, default to the full screen width.
        if (adWidthPixels == 0f) {
            adWidthPixels = outMetrics.widthPixels.toFloat()
        }
        val adWidth = (adWidthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(requireContext(), adWidth)
    }
}