/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.video.test;

import java.io.FileNotFoundException;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.video.VideoPlayer;
import com.badlogic.gdx.video.VideoPlayerCreator;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class GdxVideoTest extends ApplicationAdapter {
	OrthographicCamera camera;
	VideoPlayer videoPlayer;
	ImmediateModeRenderer20 renderer;

	//@off
	String vertexShaderCode = 
		"#define highp\n" +
		"attribute highp vec4 a_position; \n" +
		"attribute highp vec2 a_texCoord0;" +
		"uniform highp mat4 u_projModelView;" +
		"varying highp vec2 v_texCoord0;" +
		"void main() \n" +
		"{ \n" +
		" gl_Position = u_projModelView * a_position; \n" +
		" v_texCoord0 = a_texCoord0;\n" +
		"} \n";

	String fragmentShaderCode = 
		"#define highp\n" +
		"//#extension GL_OES_EGL_image_external : require\n" +
		"uniform sampler2D u_sampler0;" +
		"varying highp vec2 v_texCoord0;" +
		"void main()                 \n" +
		"{                           \n" +
		"  gl_FragColor = texture2D(u_sampler0, v_texCoord0);    \n" +
		"}";
	//@on

	private ShaderProgram shader;
	private Matrix4 transform;
		
	@Override
	public void create () {
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
		camera = new OrthographicCamera();
		videoPlayer = VideoPlayerCreator.createVideoPlayer();
		videoPlayer.setOnCompletionListener(new VideoPlayer.CompletionListener() {
			@Override
			public void onCompletionListener (FileHandle file) {
				Gdx.app.log("VideoTest", file.name() + " fully played.");
			}
		});
		videoPlayer.setOnVideoSizeListener(new VideoPlayer.VideoSizeListener() {
			@Override
			public void onVideoSize (float width, float height) {
				Gdx.app.log("VideoTest", "The video has a size of " + width + "x" + height + ".");
			}
		});
		shader = new ShaderProgram(vertexShaderCode, fragmentShaderCode);
		if(!shader.isCompiled()) throw new GdxRuntimeException(shader.getLog());
		
		renderer = new ImmediateModeRenderer20(4, false, false, 1);
		renderer.setShader(shader);
		transform = new Matrix4().setToOrtho2D(0, 0, 1, 1);
	}

	@Override
	public void render () {
		if (Gdx.input.justTouched()) {
			try {
				videoPlayer.play(Gdx.files.internal("libGDX - It's Good For You!.webm"));
			} catch (FileNotFoundException e) {
				System.out.println("Oh no!");
			}
		}

		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Texture frame = videoPlayer.getTexture();
		
		
		if(frame != null){
			shader.bind();
			Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
			Gdx.gl.glBindTexture(frame.glTarget, frame.getTextureObjectHandle());
			shader.setUniformi("u_sampler0", 0);
			
			renderer.begin(transform, GL20.GL_TRIANGLE_STRIP);
			renderer.texCoord(0, 1); renderer.vertex(0, 0, 0);
			renderer.texCoord(1, 1); renderer.vertex(1, 0, 0);
			renderer.texCoord(0, 0); renderer.vertex(0, 1, 0);
			renderer.texCoord(1, 0); renderer.vertex(1, 1, 0);
			renderer.end();
		}
	}

	@Override
	public void pause () {
		videoPlayer.pause();
	}

	@Override
	public void resume () {
		videoPlayer.resume();
	}

	@Override
	public void dispose () {
		videoPlayer.dispose();
	}
}
