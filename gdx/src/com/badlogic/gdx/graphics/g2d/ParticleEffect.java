/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
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
package com.badlogic.gdx.graphics.g2d;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class ParticleEffect {
	private final Array<ParticleEmitter> emitters;

	public ParticleEffect () {
		emitters = new Array(true, 8, ParticleEmitter.class);
	}

	public ParticleEffect (ParticleEffect effect) {
		emitters = new Array(true, effect.emitters.size, ParticleEmitter.class);
		for (int i = 0, n = effect.emitters.size; i < n; i++)
			emitters.add(new ParticleEmitter(effect.emitters.items[i]));
	}

	public void start () {
		for (int i = 0, n = emitters.size; i < n; i++)
			emitters.items[i].start();
	}

	public void draw (SpriteBatch spriteBatch, float delta) {
		for (int i = 0, n = emitters.size; i < n; i++)
			emitters.items[i].draw(spriteBatch, delta);
	}

	public void allowCompletion () {
		for (int i = 0, n = emitters.size; i < n; i++)
			emitters.items[i].allowCompletion();
	}

	public boolean isComplete () {
		for (int i = 0, n = emitters.size; i < n; i++) {
			ParticleEmitter emitter = emitters.items[i];
			if (emitter.isContinuous()) return false;
			if (!emitter.isComplete()) return false;
		}
		return true;
	}

	public void setDuration (int duration) {
		for (int i = 0, n = emitters.size; i < n; i++) {
			ParticleEmitter emitter = emitters.items[i];
			emitter.setContinuous(false);
			emitter.duration = duration;
			emitter.durationTimer = 0;
		}
	}

	public void setPosition (float x, float y) {
		for (int i = 0, n = emitters.size; i < n; i++)
			emitters.items[i].setPosition(x, y);
	}

	public void setFlip (boolean flipX, boolean flipY) {
		for (int i = 0, n = emitters.size; i < n; i++)
			emitters.items[i].setFlip(flipX, flipY);
	}

	public Array<ParticleEmitter> getEmitters () {
		return emitters;
	}

	/**
	 * Returns the emitter with the specified name, or null.
	 */
	public ParticleEmitter findEmitter (String name) {
		for (int i = 0, n = emitters.size; i < n; i++) {
			ParticleEmitter emitter = emitters.items[i];
			if (emitter.getName().equals(name)) return emitter;
		}
		return null;
	}

	public void save (File file) {
		Writer output = null;
		try {
			output = new FileWriter(file);
			int index = 0;
			for (int i = 0, n = emitters.size; i < n; i++) {
				ParticleEmitter emitter = emitters.items[i];
				if (index++ > 0) output.write("\n\n");
				emitter.save(output);
				output.write("- Image Path -\n");
				output.write(emitter.getImagePath() + "\n");
			}
		} catch (IOException ex) {
			throw new GdxRuntimeException("Error saving effect: " + file, ex);
		} finally {
			try {
				if (output != null) output.close();
			} catch (IOException ex) {
			}
		}
	}

	public void load (FileHandle effectFile, FileHandle imagesDir) {
		loadEmitters(effectFile);
		loadEmitterImages(imagesDir);
	}

	public void load (FileHandle effectFile, TextureAtlas atlas) {
		loadEmitters(effectFile);
		loadEmitterImages(atlas);
	}

	public void loadEmitters (FileHandle effectFile) {
		InputStream input = effectFile.read();
		emitters.clear();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(input), 512);
			while (true) {
				ParticleEmitter emitter = new ParticleEmitter(reader);
				reader.readLine();
				emitter.setImagePath(reader.readLine());
				emitters.add(emitter);
				if (reader.readLine() == null) break;
				if (reader.readLine() == null) break;
			}
		} catch (IOException ex) {
			throw new GdxRuntimeException("Error loading effect: " + effectFile, ex);
		} finally {
			try {
				if (reader != null) reader.close();
			} catch (IOException ex) {
			}
		}
	}

	public void loadEmitterImages (TextureAtlas atlas) {
		for (int i = 0, n = emitters.size; i < n; i++) {
			ParticleEmitter emitter = emitters.items[i];
			String imagePath = emitter.getImagePath();
			if (imagePath == null) continue;
			String imageName = new File(imagePath.replace('\\', '/')).getName();
			int lastDotIndex = imageName.lastIndexOf('.');
			if (lastDotIndex != -1) imageName = imageName.substring(0, lastDotIndex);
			Sprite sprite = atlas.createSprite(imageName);
			if (sprite == null) throw new IllegalArgumentException("SpriteSheet missing image: " + imageName);
			emitter.setSprite(sprite);
		}
	}

	public void loadEmitterImages (FileHandle imagesDir) {
		for (int i = 0, n = emitters.size; i < n; i++) {
			ParticleEmitter emitter = emitters.items[i];
			String imagePath = emitter.getImagePath();
			if (imagePath == null) continue;
			String imageName = new File(imagePath.replace('\\', '/')).getName();
			emitter.setSprite(new Sprite(loadTexture(imagesDir.child(imageName))));
		}
	}

	protected Texture loadTexture (FileHandle file) {
		return new Texture(file, false);
	}
}
