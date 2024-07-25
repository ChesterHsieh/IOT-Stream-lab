import json
import time
import pika
from faker import Faker

fake = Faker()


def generate_fake_weather_sensor_data():
    return {
        'sensorId': fake.uuid4(),
        'timestamp': fake.date_time_this_year().isoformat(),
        'temperature': round(fake.random_number(digits=2), 2),
        'humidity': fake.random_int(min=0, max=100),
        'windSpeed': round(fake.random_number(digits=2), 2),
        'atmosphericPressure': round(fake.random_number(digits=4), 2)
    }


def generate_fake_motion_sensor_data():
    return {
        'sensorId': fake.uuid4(),
        'timestamp': fake.date_time_this_year().isoformat(),
        'motionDetected': fake.boolean(),
        'motionIntensity': fake.random_int(min=1, max=10)
    }


def main():
    connection = pika.BlockingConnection(
        pika.ConnectionParameters(host='rabbitmq'))
    channel = connection.channel()

    channel.queue_declare(queue='solar_data')

    while True:
        sensor_data = generate_fake_weather_sensor_data()
        motion_data = generate_fake_motion_sensor_data()
        channel.basic_publish(exchange='',
                              routing_key='weather',
                              body=json.dumps(sensor_data))
        channel.basic_publish(exchange='', routing_key='motion', body=json.dumps(motion_data))
        time.sleep(1)

    connection.close()


if __name__ == '__main__':
    main()
