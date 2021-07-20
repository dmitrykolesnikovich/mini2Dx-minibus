/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 See AUTHORS file
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
package org.mini2Dx.minibus;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

public class MessageBusTest implements MessageHandler {
	private final CountDownLatch countDownLatch = new CountDownLatch(4);
	private final Queue<MessageExchange> exchangeQueue = new ConcurrentLinkedQueue<MessageExchange>();
	private final Queue<String> messagesReceived = new ConcurrentLinkedQueue<>();

	private MessageBus messageBus;

	@Test
	public void testMessageTransmissionAllocateOnImmediateExchange() {
		messageBus = new MessageBus();
		messageBus.createImmediateExchange(MessageBusTest.this);
		messageBus.createImmediateExchange(MessageBusTest.this);

		final String messageType = "TEST";
		messageBus.broadcast(messageType);
		messageBus.update(0.16f);
		Assert.assertEquals(2, exchangeQueue.size());
		Assert.assertEquals(2, messagesReceived.size());

		while(!messagesReceived.isEmpty()) {
			Assert.assertEquals(messageType, messagesReceived.poll());
		}
	}

	@Test
	public void testConcurrentExchangeCreate() {
		messageBus = new MessageBus();
		messageBus.createOnUpdateExchange(MessageBusTest.this);

		final Thread thread1 = new Thread(new Runnable() {
			@Override
			public void run() {
				countDownLatch.countDown();

				try {
					countDownLatch.await();
				} catch (Exception e) {}

				for(int i = 0; i < 10000; i++) {
					messageBus.update(0.016f);
				}
			}
		});
		final Thread thread2 = new Thread(new Runnable() {
			@Override
			public void run() {
				countDownLatch.countDown();

				try {
					countDownLatch.await();
				} catch (Exception e) {}

				for(int i = 0; i < 10000; i++) {
					messageBus.broadcast("MESSAGE-TYPE");
				}
			}
		});
		final Thread thread3 = new Thread(new Runnable() {
			@Override
			public void run() {
				countDownLatch.countDown();

				try {
					countDownLatch.await();
				} catch (Exception e) {}

				for(int i = 0; i < 10000; i++) {
					final MessageExchange updateExchange = messageBus.createOnUpdateExchange();
					exchangeQueue.offer(updateExchange);
					final MessageExchange immediateExchange = messageBus.createImmediateExchange();
					exchangeQueue.offer(immediateExchange);
				}
			}
		});
		final Thread thread4 = new Thread(new Runnable() {
			@Override
			public void run() {
				countDownLatch.countDown();

				try {
					countDownLatch.await();
				} catch (Exception e) {}

				for(int i = 0; i < 10000 * 10000; i++) {
					while(!exchangeQueue.isEmpty()) {
						final MessageExchange messageExchange = exchangeQueue.poll();
						messageExchange.dispose();
					}
				}
			}
		});

		thread1.start();
		thread2.start();
		thread3.start();
		thread4.start();

		try {
			thread1.join();
			thread2.join();
			thread3.join();
			thread4.join();
		} catch (Exception e) {
		}

	}

	@Override
	public void onMessageReceived(String messageType, MessageExchange source, MessageExchange receiver, MessageData messageData) {
		final MessageExchange updateExchange = messageBus.createOnUpdateExchange(MessageBusTest.this);
		exchangeQueue.offer(updateExchange);
		messagesReceived.add(messageType);
	}
}
