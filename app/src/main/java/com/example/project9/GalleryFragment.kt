package com.example.project9

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.example.project9.databinding.FragmentGalleryBinding
import android.net.Uri
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.LinearLayoutManager

class GalleryFragment : Fragment(), SensorEventListener
{

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SharedViewModel by viewModels({requireActivity()})
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var lastUpdate: Long = 0
    private var lastX: Float = 0.0f
    private var lastY: Float = 0.0f
    private var lastZ: Float = 0.0f
    private val shakeThreshold = 800

    /**
     * Initializes the sensor manager and accelerometer sensor when the fragment is created
     */
    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    /**
     * Registers the sensor event listener when the fragment resumes
     */
    override fun onResume()
    {
        super.onResume()
        accelerometer?.also { accel -> sensorManager?.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL)}
    }

    /**
     * Unregisters the sensor event listener when the fragment pauses
     */
    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    /**
     * Handles changes in sensor readings. Used for detecting shaking of the device
     *
     * @param event The sensor event with new readings
     */
    override fun onSensorChanged(event: SensorEvent?) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastUpdate > 100)
        {
            val diffTime = currentTime - lastUpdate
            lastUpdate = currentTime

            val x = event?.values?.get(0) ?: 0f
            val y = event?.values?.get(1) ?: 0f
            val z = event?.values?.get(2) ?: 0f

            val speed = Math.abs(x + y + z - lastX - lastY - lastZ) / diffTime * 10000

            if (speed > shakeThreshold) {view?.findNavController()?.navigate(R.id.action_galleryFragment_to_cameraFragment)}

            lastX = x
            lastY = y
            lastZ = z
        }
    }

    /**
     * Callback for changes in the sensor's accuracy. This implementation does nothing
     *
     * @param sensor The sensor being observed
     * @param accuracy The new accuracy of this sensor
     */
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    /**
     * Inflates the view for this fragment
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here
     * @return The View for the fragment's UI
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Called immediately after onCreateView
     *
     * @param view The View returned by onCreateView(LayoutInflater, ViewGroup, Bundle)
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        viewModel.authenticationState.observe(viewLifecycleOwner)
        { authState ->
            if (authState == AuthenticationState.UNAUTHENTICATED)
            {
                binding.recyclerViewImages.adapter = ImageAdapter(emptyList()) {}
                binding.textViewMessage.visibility = View.VISIBLE
            }
        }

        viewModel.imagesFlow.observe(viewLifecycleOwner)
        { images ->
            Log.d("GalleryFragment", "Images received: $images")
            if (images.isEmpty())
            {
                binding.textViewMessage.visibility = View.VISIBLE
            }
            else {
                binding.textViewMessage.visibility = View.GONE
                binding.recyclerViewImages.layoutManager = LinearLayoutManager(context)
                binding.recyclerViewImages.adapter = ImageAdapter(images)
                { uri ->
                    viewModel.selectImageUri(uri)
                    view.findNavController().navigate(R.id.action_galleryFragment_to_imageViewFragment)
                }
            }
        }

        val callback = object : OnBackPressedCallback(true)
        {
            override fun handleOnBackPressed()
            {
                viewModel.signOut()
                view.findNavController().navigate(R.id.action_galleryFragment_to_loginFragment)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

    }


    /**
     * Called when the view is destroyed
     */
    override fun onDestroyView()
    {
        super.onDestroyView()
        _binding = null
    }
}