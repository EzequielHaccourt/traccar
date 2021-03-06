/*
 * Copyright 2015 Anton Tananaev (anton.tananaev@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.protocol;

import java.net.SocketAddress;
import java.util.regex.Pattern;
import org.jboss.netty.channel.Channel;
import org.traccar.BaseProtocolDecoder;
import org.traccar.helper.DateBuilder;
import org.traccar.helper.Parser;
import org.traccar.helper.PatternBuilder;
import org.traccar.helper.UnitsConverter;
import org.traccar.model.Event;
import org.traccar.model.Position;

public class Xt013ProtocolDecoder extends BaseProtocolDecoder {

    public Xt013ProtocolDecoder(Xt013Protocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .number("HI,d+").optional()
            .text("TK,")
            .number("(d+),")                     // imei
            .number("(dd)(dd)(dd)")              // date (yymmdd)
            .number("(dd)(dd)(dd),")             // time
            .number("([+-]d+.d+),")              // latitude
            .number("([+-]d+.d+),")              // longitude
            .number("(d+),")                     // speed
            .number("(d+),")                     // course
            .number("d+,")
            .number("(d+),")                     // altitude
            .expression("([FL]),")               // gps fix
            .number("d+,")
            .number("(d+),")                     // gps level
            .number("x+,")
            .number("x+,")
            .number("(d+),")                     // gsm level
            .expression("[^,]*,")
            .number("(d+.d+),")                  // battery
            .number("(d),")                      // charging
            .any()
            .compile();

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        Parser parser = new Parser(PATTERN, (String) msg);
        if (!parser.matches()) {
            return null;
        }

        Position position = new Position();
        position.setProtocol(getProtocolName());

        if (!identify(parser.next(), channel, remoteAddress)) {
            return null;
        }
        position.setDeviceId(getDeviceId());

        DateBuilder dateBuilder = new DateBuilder()
                .setDate(parser.nextInt(), parser.nextInt(), parser.nextInt())
                .setTime(parser.nextInt(), parser.nextInt(), parser.nextInt());
        position.setTime(dateBuilder.getDate());

        position.setLatitude(parser.nextDouble());
        position.setLongitude(parser.nextDouble());
        position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble()));
        position.setCourse(parser.nextDouble());
        position.setAltitude(parser.nextDouble());
        position.setValid(parser.next().equals("F"));

        position.set(Event.KEY_GPS, parser.next());
        position.set(Event.KEY_GSM, parser.next());
        position.set(Event.KEY_BATTERY, parser.next());
        position.set(Event.KEY_CHARGE, parser.next());

        return position;
    }

}
