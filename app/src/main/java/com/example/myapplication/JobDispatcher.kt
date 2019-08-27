package com.example.myapplication


//    <service android:name=".MyJobService" android:exported="false">
//    <intent-filter>
//    <action android:name="com.firebase.jobdispatcher.ACTION_EXECUTE"/>
//    </intent-filter>
//    </service>
//
//
//import android.app.job.JobParameters;
//import android.app.job.JobService;
//import android.widget.Toast;
//import com.firebase.jobdispatcher.*
//import com.firebase.jobdispatcher.JobParameters
//import com.firebase.jobdispatcher.JobService
//import android.widget.Toast

//public class MyJobService extends JobService {
//    @Override
//    public boolean onStartJob(JobParameters jobParameters) {
//
//        Toast.makeText(this, "onStart", Toast.LENGTH_SHORT).show();
//        return false;
//    }
//
//    @Override
//    public boolean onStopJob(JobParameters jobParameters) {
//
//        Toast.makeText(this, "onStop", Toast.LENGTH_SHORT).show();
//        return false;
//    }
//}
//class MyJobServiceJava:JobService() {
//    override fun onStartJob(p0: JobParameters?): Boolean {
//
//        Toast.makeText(this, "onStart", Toast.LENGTH_SHORT).show()
//        return false
//    }
//
//    override fun onStopJob(p0: JobParameters?): Boolean {
//
//        Toast.makeText(this, "onStop", Toast.LENGTH_SHORT).show()
//        return true
//    }
//}


//
//    override fun onPause() {
//
//        val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(this))
//        val myExtraBundle = Bundle()
//
//        myExtraBundle.putString("key","value")
//
//        val myJob = dispatcher
//            .newJobBuilder()
//            .setService(MyJobServiceJava::class.java)
//            .setTag("unique_tag")
//            .setRecurring(true)
//            .setLifetime(Lifetime.FOREVER)
//            .setTrigger(Trigger.executionWindow(0,60))
//            .setReplaceCurrent(false)
//            .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
//            .setExtras(myExtraBundle)
//            .build()
//
//        dispatcher.mustSchedule(myJob)
//
//        //FireBaseJobDispatcher dispatcher
//
//        super.onPause()
//    }