/**
 * The MIT License (MIT)
 * 
 * Copyright (c) 2016 See AUTHORS file
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.mini2Dx.minibus.transmission;

import org.mini2Dx.minibus.MessageBus;
import org.mini2Dx.minibus.util.SynchronizedQueue;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * An object pool of {@link MessageTransmission} instances to reduce memory allocations
 */
public class MessageTransmissionPool {
	private final Queue<MessageTransmission> pool = new SynchronizedQueue<MessageTransmission>();

	/**
	 * Allocates a new {@link MessageTransmission} from the pool
	 * @return
	 */
	public MessageTransmission allocate() {
		final MessageTransmission result = pool.poll();
		if(result == null) {
			return new MessageTransmission(this);
		}
		return result;
	}
	
	/**
	 * Releases a {@link MessageTransmission} back to the pool
	 * @param messageTransmission
	 */
	public void release(MessageTransmission messageTransmission) {
		pool.offer(messageTransmission);
	}

	public int size() {
		return pool.size();
	}
}
