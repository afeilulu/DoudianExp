package cn.com.xinli.android.doudian.ui;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import cn.com.xinli.android.doudian.R;
import cn.com.xinli.android.doudian.ui.tv.HomeBlueActivity;
import cn.com.xinli.android.doudian.utils.Authenticate;
import cn.com.xinli.android.doudian.utils.DateCompare;
import cn.com.xinli.android.doudian.utils.MD5;
import cn.com.xinli.android.doudian.utils.UpgradePackage;
import cn.com.xinli.android.doudian.utils.Utils;

public class WelcomeActivity extends Activity {
    public final static int DOWNLOAD_PROGRESS = 1;
    private final String TAG = "WelcomeActivity";
    private TextView hint;
    private ProgressBar progressBar;
    private WelcomeTask task;
    private DownloadManager dMgr;
    private long lastDownloadId;
    private String saveFileName;
    private String md5;
    private boolean downloadCompleteFlag;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            Log.i(TAG, "下载升级包完成！");
            downloadCompleteFlag = true;
        }

    };
    private Button btnEnter;
    private Button btnCanel;
    private String mErrorCode;
    private String versionName;
    private String dateInVersionName;
    private AutoTimeObserver autoTimeObserver = new AutoTimeObserver(
            new Handler());

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);
        hint = (TextView) findViewById(R.id.hint);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        btnEnter = (Button) findViewById(R.id.btn_enter);
        btnEnter.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // finish();
                installIntent(WelcomeActivity.this);
            }
        });

        btnCanel = (Button) findViewById(R.id.btn_canel);
        btnCanel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "btnCanel==");

                goHome();
            }
        });

        registerReceiver(mReceiver, new IntentFilter(
                DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        getContentResolver()
                .registerContentObserver(
                        android.provider.Settings.System
                                .getUriFor(Settings.System.AUTO_TIME),
                        true, autoTimeObserver
                );
        /*try {
			versionName = getPackageManager().getPackageInfo(getPackageName(),
					0).versionName;
			String[] dateStrings = versionName.split(" ");
			if (versionName.toLowerCase().contains("expire")
					&& dateStrings.length > 0) {

				dateInVersionName = dateStrings[dateStrings.length - 1];

				int autoTime = 0;
				try {
					autoTime = android.provider.Settings.System.getInt(
							getContentResolver(), Settings.System.AUTO_TIME);
				} catch (SettingNotFoundException e) {
					finish();
				}
				Log.d(TAG, "autoTime = " + autoTime);

				if (autoTime == 0) {
					Uri uri = android.provider.Settings.System
							.getUriFor(Settings.System.AUTO_TIME);
					android.provider.Settings.System.putInt(
							getContentResolver(), Settings.System.AUTO_TIME, 1);
					getContentResolver().notifyChange(uri, autoTimeObserver);
				} else {
					DateCompare dateCompare = new DateCompare(dateInVersionName);
					if (dateCompare.isExpired()) {
						Toast.makeText(this, R.string.expired_tip,
								Toast.LENGTH_LONG).show();
						finish();
					} else {
						String tmpStr = getString(R.string.expired_on,
								dateInVersionName);
						Toast.makeText(this, tmpStr, Toast.LENGTH_LONG).show();

						progressBar.setVisibility(View.INVISIBLE);
						progressBar.postDelayed(new Runnable() {

							@Override
							public void run() {
								goHome();

							}
						}, 3000);
					}
				}
			} else {
				dMgr = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
				task = new WelcomeTask();
				task.execute(this);
			}
		} catch (NameNotFoundException e) {
			finish();
		}*/
        goHome();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        getContentResolver().unregisterContentObserver(autoTimeObserver);
        unregisterReceiver(mReceiver);
    }

    private void goHome() {
        Intent intent = new Intent(this, HomeBlueActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult==resultCode=" + resultCode
                + "==requestCode==" + requestCode);

    }

    private void addTask(String[] checkInfo) {
        try {
            Log.d(TAG, "download uri = " + checkInfo[1]);
            Uri uri = Uri.parse(checkInfo[1]);
            String[] urlSplit = checkInfo[1].split("/");
            saveFileName = urlSplit[urlSplit.length - 1];
            Log.i(TAG, "saveFileName="
                    + Environment.getExternalStorageDirectory().getPath()
                    + "/Download/" + saveFileName);
            File file = new File(Environment.getExternalStorageDirectory()
                    .getPath() + "/Download/" + saveFileName);
            if (file.exists())
                file.delete();

            DownloadManager.Request dmReq = new DownloadManager.Request(uri);
            dmReq.setTitle(saveFileName);
            dmReq.setDescription("Download for Update apk");
            Log.i(TAG, "Environment.DIRECTORY_DOWNLOADS="
                    + Environment.DIRECTORY_DOWNLOADS + ",saveFileName="
                    + saveFileName);
            dmReq.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS, saveFileName);
            lastDownloadId = dMgr.enqueue(dmReq);
            Log.i(TAG, "lastDownloadId=" + lastDownloadId);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private boolean checkMd5() {
        try {

            // return true;

            if (!TextUtils.isEmpty(md5)
                    && !MD5.checkMd5(md5, Environment
                    .getExternalStorageDirectory().getPath()
                    + "/Download/" + saveFileName)) {
                Log.d(TAG, "md5 check failed");
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }

    }

    private void installIntent(Context context) {
        try {
            Log.i(TAG, "installIntent=Environment.DIRECTORY_DOWNLOADS=="
                    + Environment.getExternalStorageDirectory().getPath()
                    + "/Download/,saveFileName=" + saveFileName);
            File apkfile = new File(Environment.getExternalStorageDirectory()
                    .getPath() + "/Download/" + saveFileName);
            if (!apkfile.exists()) {
                Log.d(TAG, "下载文件未找到");
            }
            Log.i(TAG,
                    "installIntent=apkfile==" + "file://" + apkfile.toString());

            Uri uri = Uri.parse("file://" + apkfile.toString());
            Intent intentInstall = new Intent(Intent.ACTION_VIEW);
            // be careful of this flag
            intentInstall.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intentInstall.setDataAndType(uri,
                    "application/vnd.android.package-archive");
            // startActivity(intentInstall);
            startActivityForResult(intentInstall, 0);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void tastStatus(long lastDownloadId) {
        Query myDownloadQuery = new Query();
        myDownloadQuery.setFilterById(lastDownloadId);
        Cursor cursor = dMgr.query(myDownloadQuery);
        if (cursor != null && cursor.moveToFirst()) {
            int _id = cursor.getColumnIndex(DownloadManager.COLUMN_ID);
            int _name = cursor
                    .getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
            int _total = cursor
                    .getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
            int _size = cursor
                    .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
            long id = cursor.getLong(_id);
            String name = cursor.getString(_name);
            long total = cursor.getLong(_total);
            long size = cursor.getLong(_size);

            Log.i(TAG,
                    "name="
                            + name
                            + ",size="
                            + size
                            + ",total="
                            + total
                            + "==status"
                            + cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            );

            Log.i(TAG, "下载" + size / total + "%");
            hint.setText(getResources().getString(R.string.downloading) + size
                    / total + getResources().getString(R.string.waiting));
        }

    }

    /**
     * 检查有没有升级包
     *
     * @return null 没有升级包
     */
    private String[] checkUpdateVersion(String mac) {
        try {
            int currentVersion = getCurrentVersion();
            Log.i(TAG, "currentVersion==" + currentVersion);

            if (currentVersion <= 0) {
                return null;
            }

            UpgradePackage upgradePackage = Authenticate.getUpgradePackages(
                    currentVersion, mac);
            if (upgradePackage != null) {
                Log.i(TAG, "upgradePackage != null");
                int version = upgradePackage.getCurVersion();
                Log.i(TAG, "upgradePackage=" + version);

                if (currentVersion >= version) {
                    Log.i(TAG, "currentVersion >= version");
                    return null;
                } else {
                    md5 = upgradePackage.getMd5();
                    String apkUrl = "";
                    if (upgradePackage.getPackages() != null)
                        apkUrl = Authenticate.UPDATEAPKHOST
                                + upgradePackage.getPackages();
                    else
                        apkUrl = null;
                    Log.i(TAG, "apkUrl==" + apkUrl + ",packages="
                            + upgradePackage.getPackages());

                    String description = upgradePackage.getAnnouncement();

                    String[] checkInfo = new String[4];
                    checkInfo[0] = md5;
                    checkInfo[1] = apkUrl;
                    checkInfo[2] = description;
                    checkInfo[3] = String.valueOf(version);
                    return checkInfo;
                }
            } else {
                Log.i(TAG, "upgradePackage == null=");
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private int getCurrentVersion() {
        try {
            PackageManager pm = getPackageManager();
            PackageInfo pinfo = pm.getPackageInfo(getPackageName(),
                    PackageManager.GET_CONFIGURATIONS);
            return pinfo.versionCode;// 获取在AndroidManifest.xml中配置的版本号

        } catch (PackageManager.NameNotFoundException e) {
            e.fillInStackTrace();
            return 0;
        }
    }

    private class WelcomeTask extends AsyncTask<Context, String, Integer> {

        private Context context;

        @Override
        protected Integer doInBackground(Context... params) {
            int result = 0;
            context = params[0];
            Utils utils = new Utils(context);
            publishProgress(
                    getResources().getString(R.string.checking_network), "10");
            boolean network = utils.checkNetworkInfo();
            if (network) {
                publishProgress(getResources()
                        .getString(R.string.checking_auth), "20");
                String mac = "";

                /**
                 * 增加升级开始
                 */
                publishProgress(
                        getResources().getString(R.string.checking_pgrade),
                        "30");
                String[] checkInfo = checkUpdateVersion(mac);
                if (checkInfo == null) {
                    publishProgress(getResources()
                            .getString(R.string.no_pgrade), "100");
                    result = 1;
                } else {
                    publishProgress(
                            getResources().getString(R.string.ready_download),
                            "50");
                    Log.d(TAG, "------------apkUrl----------:" + checkInfo[1]
                            + "==md5==" + checkInfo[0]);

                    addTask(checkInfo);
                    // 监听下载进度
                    // handler.sendEmptyMessage(DOWNLOAD_PROGRESS);

                    Query myDownloadQuery = new Query();
                    myDownloadQuery.setFilterById(lastDownloadId);
                    Cursor cursor = null;
                    long currTime = System.currentTimeMillis();

                    long consuming = 0;
                    while (true) {
                        consuming = (System.currentTimeMillis() - currTime) / 1000 / 60;

                        if (consuming >= 30) {

                            publishProgress(
                                    getResources().getString(
                                            R.string.download_timeout), "100"
                            );
                            result = 1;
                            break;
                        }
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        cursor = dMgr.query(myDownloadQuery);

                        if (cursor != null && cursor.moveToFirst()) {
                            int _id = cursor
                                    .getColumnIndex(DownloadManager.COLUMN_ID);
                            int _name = cursor
                                    .getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
                            int _total = cursor
                                    .getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
                            int _size = cursor
                                    .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
                            long id = cursor.getLong(_id);
                            String name = cursor.getString(_name);
                            long total = cursor.getLong(_total);
                            long size = cursor.getLong(_size);
                            int _status = cursor
                                    .getColumnIndex(DownloadManager.COLUMN_STATUS);
                            int status = cursor.getInt(_status);
                            double progress = ((double) size / (double) total) * 100;
                            if (progress < 1)
                                progress = 1;
                            java.text.DecimalFormat df = new java.text.DecimalFormat(
                                    "#");
                            Log.d(TAG,
                                    "download status = df.format(progress)=="
                                            + df.format(progress)
                            );

                            publishProgress(
                                    getResources().getString(
                                            R.string.downloading)
                                            + df.format(progress)
                                            + getResources().getString(
                                            R.string.waiting),
                                    String.valueOf(df.format(progress))
                            );

                            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                if (checkMd5()) {// md5验证通过
                                    publishProgress(
                                            getResources().getString(
                                                    R.string.down_success),
                                            "100"
                                    );
                                    result = 2;
                                    break;
                                } else {
                                    publishProgress(
                                            getResources().getString(
                                                    R.string.down_failure),
                                            "100"
                                    );
                                    result = 1;
                                    break;
                                }

                            }

                        }
                    }

                }

            } else {
                publishProgress(
                        getResources().getString(R.string.chk_network_err),
                        "50");
            }
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (result.intValue() == 0) {
                // finish();
            } else if (result.intValue() == 1) {
                goHome();
            } else {
                btnEnter.setVisibility(View.VISIBLE);
                btnCanel.setVisibility(View.VISIBLE);
                btnEnter.setFocusable(true);
                btnEnter.setFocusableInTouchMode(true);
                btnEnter.requestFocus();
            }

        }

        @Override
        protected void onProgressUpdate(String... values) {
            // TODO Auto-generated method stub
            hint.setText(values[0]);
            progressBar.setProgress(Integer.parseInt(values[1]));
        }

    }

    public class AutoTimeObserver extends ContentObserver {

        public AutoTimeObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            Log.d("aaaa", "selfchange = " + selfChange);
            DateCompare dateCompare = new DateCompare(dateInVersionName);
            if (dateCompare.isExpired()) {
                Toast.makeText(WelcomeActivity.this, R.string.expired_tip,
                        Toast.LENGTH_LONG).show();
                finish();
            } else {
                String tmpStr = getString(R.string.expired_on,
                        dateInVersionName);
                Toast.makeText(WelcomeActivity.this, tmpStr, Toast.LENGTH_LONG)
                        .show();

                progressBar.setVisibility(View.INVISIBLE);
                progressBar.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        goHome();

                    }
                }, 3000);
            }
        }
    }
}