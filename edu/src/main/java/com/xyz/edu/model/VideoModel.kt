package com.xyz.edu.model

import android.content.Context
import com.xyz.base.app.mvp.BaseModel
import com.xyz.edu.contract.IHomeC
import com.xyz.edu.contract.IVideoC

class VideoModel(context: Context): BaseModel(context), IVideoC.Model {
}